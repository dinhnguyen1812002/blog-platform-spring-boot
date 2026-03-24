package com.Nguyen.blogplatform.repository;

import com.Nguyen.blogplatform.Enum.EOAuthProvider;
import com.Nguyen.blogplatform.model.OAuthAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, String> {

    Optional<OAuthAccount> findByProviderAndProviderId(EOAuthProvider provider, String providerId);

    Optional<OAuthAccount> findByProviderAndEmail(EOAuthProvider provider, String email);

    List<OAuthAccount> findByUserId(String userId);

    List<OAuthAccount> findByUserIdAndIsActiveTrue(String userId);

    Optional<OAuthAccount> findByUserIdAndIsPrimaryTrue(String userId);

    boolean existsByProviderAndProviderId(EOAuthProvider provider, String providerId);

    boolean existsByUserIdAndProvider(String userId, EOAuthProvider provider);

    @Query("SELECT oa FROM OAuthAccount oa WHERE oa.user.id = :userId AND oa.provider = :provider AND oa.isActive = true")
    Optional<OAuthAccount> findActiveAccountByUserAndProvider(@Param("userId") String userId, @Param("provider") EOAuthProvider provider);

    @Query("SELECT COUNT(oa) FROM OAuthAccount oa WHERE oa.user.id = :userId AND oa.isActive = true")
    long countActiveAccountsByUserId(@Param("userId") String userId);
}
