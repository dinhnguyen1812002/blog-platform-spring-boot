package com.Nguyen.blogplatform.domain.auth.application;



import com.Nguyen.blogplatform.domain.auth.api.AuthController;
import com.Nguyen.blogplatform.domain.auth.domain.model.RefreshToken;
import com.Nguyen.blogplatform.domain.auth.dto.JwtResponse;
import com.Nguyen.blogplatform.domain.auth.dto.LoginRequest;
import com.Nguyen.blogplatform.domain.auth.dto.SignupRequest;
import com.Nguyen.blogplatform.domain.user.domain.model.Role;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
import com.Nguyen.blogplatform.domain.user.infrastructure.repository.RoleRepository;
import com.Nguyen.blogplatform.domain.user.infrastructure.repository.UserRepository;
import com.Nguyen.blogplatform.infrastructure.security.JwtUtils;
import com.Nguyen.blogplatform.shared.dto.MessageResponse;
import com.Nguyen.blogplatform.shared.enums.ERole;
import com.Nguyen.blogplatform.shared.exception.AuthException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles authentication and registration flows.
 *
 * Design decisions:
 * - Constructor injection over @Autowired fields for testability and immutability.
 * - @Transactional on registerUser so that user save + token creation are atomic.
 * - No manual password pre-check before authenticationManager.authenticate();
 *   Spring Security's DaoAuthenticationProvider already does this and throws
 *   BadCredentialsException with a safe, generic message to prevent user enumeration.
 * - Password regex validation lives here only as a last-resort guard; the canonical
 *   constraint should be declared via @Pattern on SignupRequest so Bean Validation
 *   catches it before the request even enters the service layer.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    // -------------------------------------------------------------------------
    // Login
    // -------------------------------------------------------------------------

    /**
     * Authenticates a user and returns JWT + refresh token cookies.
     *
     * NOTE: We intentionally let Spring Security throw BadCredentialsException
     * rather than doing a manual email/password check first.  Doing the check
     * manually and returning distinct "email not found" vs "wrong password"
     * messages is a user-enumeration vulnerability.
     */
    public ResponseEntity<JwtResponse> authenticateUser(@Valid LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = extractRoleNames(userDetails);

        String jwtToken = jwtUtils.generateJwtToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        HttpHeaders headers = buildAuthCookieHeaders(authentication, refreshToken);

        JwtResponse body = new JwtResponse(
                jwtToken,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getSlug(),
                userDetails.getAvatar(),
                roles);

        log.debug("User '{}' authenticated successfully with roles: {}", userDetails.getEmail(), roles);
        return ResponseEntity.ok().headers(headers).body(body);
    }

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    /**
     * Registers a new user and auto-logs them in within the same transaction.
     * If the post-registration login fails for any reason, the transaction is
     * NOT rolled back — the user account is kept; only the JWT minting fails.
     */
    @Transactional
    public ResponseEntity<JwtResponse> registerUser(@Valid SignupRequest signUpRequest) {
        validateNewUser(signUpRequest);

        User user = buildUser(signUpRequest);
        User savedUser = userRepository.save(user);

        // Auto-login immediately after successful registration
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signUpRequest.getEmail(),
                        signUpRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser.getId());
        HttpHeaders headers = buildAuthCookieHeaders(authentication, refreshToken);

        List<String> roleNames = savedUser.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        JwtResponse body = new JwtResponse(
                jwt,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getSlug(),
                roleNames,
                "Registration successful! You have been automatically logged in.");

        log.info("New user registered and auto-logged in: '{}'", savedUser.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(body);
    }

    // -------------------------------------------------------------------------
    // Logout
    // -------------------------------------------------------------------------

    /**
     * Clears both the JWT cookie and the refresh token cookie.
     * Actual refresh token DB deletion is handled in AuthController where we
     * have access to the authenticated principal's ID. If you prefer to move
     * that here, inject SecurityContextHolder and call
     * refreshTokenService.deleteByUserId(userId) before building the response.
     */
    public ResponseEntity<MessageResponse> logoutUser() {
        ResponseCookie jwtCookie = jwtUtils.getCleanJwtCookie();
        ResponseCookie refreshCookie = refreshTokenService.getCleanRefreshTokenCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Validates uniqueness constraints before persisting a new user.
     * Throws {@link AuthException} (map to 400 via @ExceptionHandler) on conflict.
     */
    private void validateNewUser(SignupRequest req){
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new AuthException("Username is already taken.");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new AuthException("Email is already in use.");
        }
        // Belt-and-suspenders guard; the primary validation should be
        // @Pattern(regexp = "...") on SignupRequest.password so it fires
        // before we even enter the service.
        if (!isValidPassword(req.getPassword())) {
            throw new AuthException(
                    "Password must contain at least one digit, one lowercase letter, " +
                            "one uppercase letter, one special character, and no whitespace.");
        }
    }

    /** Builds and returns a fully populated {@link User} ready to be persisted. */
    private User buildUser(SignupRequest req) {
        User user = new User(
                req.getUsername(),
                req.getEmail(),
                encoder.encode(req.getPassword()));
        user.setRoles(resolveRoles(req.getRole()));
        return user;
    }

    /**
     * Resolves a set of raw role strings to {@link Role} entities.
     * Defaults to {@code ROLE_USER} when no roles are specified.
     */
    private Set<Role> resolveRoles(Set<String> requestedRoles) {
        if (requestedRoles == null || requestedRoles.isEmpty()) {
            return Set.of(findRole(ERole.ROLE_USER));
        }

        Set<Role> roles = new HashSet<>();
        for (String roleName : requestedRoles) {
            ERole eRole = switch (roleName) {
                case "ROLE_ADMIN"  -> ERole.ROLE_ADMIN;
                case "ROLE_AUTHOR" -> ERole.ROLE_AUTHOR;
                default            -> ERole.ROLE_USER;
            };
            roles.add(findRole(eRole));
        }
        return roles;
    }

    private Role findRole(ERole eRole) {
        return roleRepository.findByName(eRole)
                .orElseThrow(() -> new RuntimeException("Role not found: " + eRole));
    }

    /** Builds the Set-Cookie headers for both the JWT and refresh token cookies. */
    private HttpHeaders buildAuthCookieHeaders(Authentication authentication, RefreshToken refreshToken) {
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(authentication);
        ResponseCookie refreshCookie = refreshTokenService.generateRefreshTokenCookie(refreshToken.getToken());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return headers;
    }

    /** Extracts string role names from the authenticated principal's authorities. */
    private List<String> extractRoleNames(UserDetailsImpl userDetails) {
        return userDetails.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    /**
     * Password complexity guard.
     * Prefer moving this regex to a Bean Validation annotation on the request DTO
     * so validation happens at the controller layer without touching service code.
     */
    private boolean isValidPassword(String password) {
        return password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{6,}$");
    }
}