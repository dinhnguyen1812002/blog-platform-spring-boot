package com.Nguyen.blogplatform.controller.OAuth;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oauth")
public class OAuthDemoController {

    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> publicEndpoint() {
        return ResponseEntity.ok(
            Map.of("message", "Public OAuth endpoint", "status", "ok")
        );
    }

    @GetMapping("/user")
    @PreAuthorize("hasAuthority('SCOPE_posts.read')")
    public ResponseEntity<Map<String, Object>> userEndpoint(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
            Map.of(
                "message",
                "Authenticated with posts.read scope",
                "subject",
                jwt.getSubject(),
                "scopes",
                jwt.getClaimAsStringList("scope")
            )
        );
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('SCOPE_admin')")
    public ResponseEntity<Map<String, Object>> adminEndpoint(
        JwtAuthenticationToken authentication
    ) {
        return ResponseEntity.ok(
            Map.of(
                "message",
                "Admin scope granted",
                "authorities",
                authentication.getAuthorities()
            )
        );
    }

    @GetMapping("/token-info")
    public ResponseEntity<Map<String, Object>> tokenInfo(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
            Map.of(
                "issuer",
                jwt.getIssuer(),
                "subject",
                jwt.getSubject(),
                "expiresAt",
                jwt.getExpiresAt(),
                "issuedAt",
                jwt.getIssuedAt()
            )
        );
    }
}
