package com.Nguyen.blogplatform.security;

import java.security.Key;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import com.Nguyen.blogplatform.service.UserDetailsImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Null;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.web.util.WebUtils;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${blog.app.jwtSecret}")
    private String jwtSecret;

    @Value("${blog.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${blog.app.jwtCookieName}")
    private String jwtCookie;

    public String getJwtCookie() {
        return jwtCookie;
    }

//    public String generateJwtToken(Authentication authentication) {
//
//        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
//
//        return Jwts.builder()
//
//                .setSubject((userPrincipal.getUsername()))
//                .claim("userid", userPrincipal.getId())
//                .claim("roles",
//                 userPrincipal.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
//                .setIssuedAt(new Date())
//                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
//                .signWith(key(), SignatureAlgorithm.HS256)
//                .compact();
//    }

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return generateTokenFromUserId(userPrincipal.getId(), userPrincipal.getEmail());
    }

    public String generateTokenFromUserId(String userId, String email) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

//    public String getUserIdFromJwtToken(String token) {
//        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
//    }

    public ResponseCookie generateJwtCookie(Authentication authentication) {
        String jwt = generateJwtToken(authentication);
        return ResponseCookie.from(jwtCookie, jwt)
                .secure(true)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Lax")
                .httpOnly(true).build();

    }

    public String getJwtFromCookies(HttpServletRequest request) {
        return Optional
                .ofNullable(WebUtils.getCookie(request, jwtCookie))
                .map(Cookie::getValue).orElse(null);
    }

//    public ResponseCookie generateJwtCookie(Authentication authentication) {
//        String jwt = generateJwtToken(authentication);
//
//        return ResponseCookie.from(jwtCookie, jwt)
//                .secure(true)
//                .path("/")
//                .maxAge(24 * 60 * 60)
//                .sameSite("Lax")
//                .httpOnly(true).build();
//
//    }

    public ResponseCookie getCleanJwtCookie() {

        return ResponseCookie.from(jwtCookie, null).path("/api").build();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUserIdFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject(); // ✅ Đúng chỗ bạn đã ghi id
    }


    public String getRolesFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("roles", String.class);
    }

    public String getUsernameFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("username", String.class);
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    // Debug method to print all claims in a JWT token
    public void debugJwtClaims(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            logger.info("=== JWT Claims Debug ===");
            logger.info("Subject: {}", claims.getSubject());
            logger.info("User ID: {}", claims.get("userid"));
            logger.info("Username: {}", claims.get("username"));
            logger.info("Roles: '{}'", claims.get("roles"));
            logger.info("Issued At: {}", claims.getIssuedAt());
            logger.info("Expires At: {}", claims.getExpiration());
            logger.info("All Claims: {}", claims);
            logger.info("========================");
        } catch (Exception e) {
            logger.error("Error debugging JWT claims: {}", e.getMessage());
        }
    }
}
