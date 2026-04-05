package com.Nguyen.blogplatform.service.apikey;

import com.Nguyen.blogplatform.dto.apikey.ApiKeyCreatedResponse;
import com.Nguyen.blogplatform.dto.apikey.ApiKeyCreateRequest;
import com.Nguyen.blogplatform.dto.apikey.ApiKeyResponse;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.model.apikey.ApiKey;

import java.util.List;
import java.util.Optional;

public interface ApiKeyService {
    ApiKeyCreatedResponse createApiKey(User user, ApiKeyCreateRequest request);
    List<ApiKeyResponse> getUserApiKeys(User user);
    void revokeApiKey(User user, String apiKeyId);
    Optional<ApiKey> validateApiKey(String apiKey, String apiSecret);
}
