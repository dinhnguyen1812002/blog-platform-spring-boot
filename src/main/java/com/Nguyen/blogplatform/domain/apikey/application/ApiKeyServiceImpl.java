package com.Nguyen.blogplatform.domain.apikey.application;



import com.Nguyen.blogplatform.domain.apikey.domain.model.ApiKey;
import com.Nguyen.blogplatform.domain.apikey.dto.ApiKeyCreateRequest;
import com.Nguyen.blogplatform.domain.apikey.dto.ApiKeyCreatedResponse;
import com.Nguyen.blogplatform.domain.apikey.dto.ApiKeyResponse;
import com.Nguyen.blogplatform.domain.apikey.infrastructure.repository.ApiKeyRepository;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public ApiKeyCreatedResponse createApiKey(User user, ApiKeyCreateRequest request) {
        String apiKey = generateRandomString(24);
        String apiSecret = generateRandomString(48);

        ApiKey key = ApiKey.builder()
                .apiKey(apiKey)
                .apiSecretHash(passwordEncoder.encode(apiSecret))
                .name(request.name())
                .user(user)
                .active(true)
                .build();

        ApiKey savedKey = apiKeyRepository.save(key);

        return new ApiKeyCreatedResponse(
                savedKey.getId(),
                savedKey.getApiKey(),
                apiSecret,
                savedKey.getName(),
                savedKey.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApiKeyResponse> getUserApiKeys(User user) {
        return apiKeyRepository.findByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void revokeApiKey(User user, String apiKeyId) {
        ApiKey apiKey = apiKeyRepository.findByIdAndUser(apiKeyId, user)
                .orElseThrow(() -> new EntityNotFoundException("API Key not found or does not belong to user"));
        
        apiKey.revoke();
        apiKeyRepository.save(apiKey);
    }

    @Override
    @Transactional
    public Optional<ApiKey> validateApiKey(String apiKey, String apiSecret) {
        return apiKeyRepository.findByApiKey(apiKey)
                .filter(ApiKey::isActive)
                .filter(key -> passwordEncoder.matches(apiSecret, key.getApiSecretHash()))
                .map(key -> {
                    key.setLastUsedAt(LocalDateTime.now());
                    return apiKeyRepository.save(key);
                });
    }

    private String generateRandomString(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, length);
    }

    private ApiKeyResponse mapToResponse(ApiKey apiKey) {
        return new ApiKeyResponse(
                apiKey.getId(),
                apiKey.getApiKey(),
                apiKey.getName(),
                apiKey.isActive(),
                apiKey.getCreatedAt(),
                apiKey.getLastUsedAt()
        );
    }
}
