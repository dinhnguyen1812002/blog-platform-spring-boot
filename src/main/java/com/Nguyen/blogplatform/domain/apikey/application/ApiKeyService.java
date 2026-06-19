package com.Nguyen.blogplatform.domain.apikey.application;




import com.Nguyen.blogplatform.domain.apikey.domain.model.ApiKey;
import com.Nguyen.blogplatform.domain.apikey.dto.ApiKeyCreateRequest;
import com.Nguyen.blogplatform.domain.apikey.dto.ApiKeyCreatedResponse;
import com.Nguyen.blogplatform.domain.apikey.dto.ApiKeyResponse;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface ApiKeyService {
    ApiKeyCreatedResponse createApiKey(User user, ApiKeyCreateRequest request);
    List<ApiKeyResponse> getUserApiKeys(User user);
    void revokeApiKey(User user, String apiKeyId);
    Optional<ApiKey> validateApiKey(String apiKey, String apiSecret);
}
