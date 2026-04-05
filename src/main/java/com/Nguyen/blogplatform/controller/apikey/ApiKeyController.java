package com.Nguyen.blogplatform.controller.apikey;

import com.Nguyen.blogplatform.dto.apikey.ApiKeyCreatedResponse;
import com.Nguyen.blogplatform.dto.apikey.ApiKeyCreateRequest;
import com.Nguyen.blogplatform.dto.apikey.ApiKeyResponse;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.repository.UserRepository;
import com.Nguyen.blogplatform.service.apikey.ApiKeyService;
import com.Nguyen.blogplatform.service.auth.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/api-keys")
@RequiredArgsConstructor
@Tag(name = "API Key Management", description = "Endpoints for users to manage their own API keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final UserRepository userRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new API key", description = "Generates a new API key and secret pair. The secret is only shown once.")
    public ApiKeyCreatedResponse createApiKey(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ApiKeyCreateRequest request) {
        
        User user = userRepository.getReferenceById(userDetails.getId());
        return apiKeyService.createApiKey(user, request);
    }

    @GetMapping
    @Operation(summary = "Get my API keys", description = "Returns a list of all active API keys belonging to the authenticated user.")
    public List<ApiKeyResponse> getMyApiKeys(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.getReferenceById(userDetails.getId());
        return apiKeyService.getUserApiKeys(user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Revoke an API key", description = "Deactivates an API key. Once revoked, it can no longer be used.")
    public void revokeApiKey(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable String id) {
        
        User user = userRepository.getReferenceById(userDetails.getId());
        apiKeyService.revokeApiKey(user, id);
    }
}
