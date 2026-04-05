package com.Nguyen.blogplatform.repository.apikey;

import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.model.apikey.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, String> {
    
    Optional<ApiKey> findByApiKey(String apiKey);
    
    List<ApiKey> findByUser(User user);
    
    Optional<ApiKey> findByIdAndUser(String id, User user);
    
    boolean existsByApiKey(String apiKey);
}
