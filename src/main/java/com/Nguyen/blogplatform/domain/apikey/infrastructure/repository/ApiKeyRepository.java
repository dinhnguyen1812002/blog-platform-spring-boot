package com.Nguyen.blogplatform.domain.apikey.infrastructure.repository;



import com.Nguyen.blogplatform.domain.apikey.domain.model.ApiKey;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
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
