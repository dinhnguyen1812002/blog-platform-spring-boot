package com.Nguyen.blogplatform.domain.auth.api;



import com.Nguyen.blogplatform.domain.auth.application.OAuthAuthService;
import com.Nguyen.blogplatform.domain.auth.dto.JwtResponse;
import com.Nguyen.blogplatform.domain.auth.dto.OAuthProfileResponse;
import com.Nguyen.blogplatform.domain.auth.dto.OAuthTokenRequest;
import com.Nguyen.blogplatform.shared.validation.annotation.WithRateLimitProtection;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthAuthService oAuthAuthService;

    @PostMapping("/verify")
    public ResponseEntity<OAuthProfileResponse> verifyToken(
            @Valid @RequestBody OAuthTokenRequest request
    ) {
        return ResponseEntity.ok(
                oAuthAuthService.verifyToken(request.provider(), request.accessToken())
        );
    }

    @PostMapping("/login")
    @WithRateLimitProtection
    public ResponseEntity<JwtResponse> loginOrRegister(
            @Valid @RequestBody OAuthTokenRequest request
    ) {
        return oAuthAuthService.loginOrRegister(request.provider(), request.accessToken());
    }
}
