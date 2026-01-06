package com.Nguyen.blogplatform.repository;

import com.Nguyen.blogplatform.Enum.NewsletterFrequency;
import com.Nguyen.blogplatform.model.Newsletter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsletterRepository extends JpaRepository<Newsletter, String> {
    
    Optional<Newsletter> findByEmail(String email);
    
    Optional<Newsletter> findByConfirmationToken(String confirmationToken);
    
    Optional<Newsletter> findBySubscriptionToken(String subscriptionToken);
    
    Boolean existsByEmail(String email);
    
    @Query("SELECT n FROM Newsletter n WHERE n.isActive = true AND n.isConfirmed = true")
    List<Newsletter> findAllActiveAndConfirmedSubscribers();
    
    @Query("SELECT n FROM Newsletter n WHERE n.isActive = true AND n.isConfirmed = true")
    Page<Newsletter> findAllActiveAndConfirmedSubscribers(Pageable pageable);
    
    @Query("SELECT n FROM Newsletter n WHERE n.isActive = true")
    List<Newsletter> findAllActiveSubscribers();
    
    @Query("SELECT n FROM Newsletter n WHERE n.isConfirmed = false")
    List<Newsletter> findAllUnconfirmedSubscribers();
    
    @Query("SELECT COUNT(n) FROM Newsletter n WHERE n.isActive = true AND n.isConfirmed = true")
    Long countActiveAndConfirmedSubscribers();
    
    @Query("SELECT n FROM Newsletter n WHERE n.email LIKE %:email%")
    List<Newsletter> findByEmailContaining(@Param("email") String email);
    long countByIsActiveTrueAndIsConfirmedTrue();

    long countBySubscribedAtBetweenAndIsActiveTrueAndIsConfirmedTrue(
            LocalDateTime start, LocalDateTime end);

    List<Newsletter> findByIsActiveTrueAndIsConfirmedTrueAndFrequency(
            NewsletterFrequency frequency
    );

    List<Newsletter> findByIsActiveTrueAndIsConfirmedTrueAndFrequencyAndLastSentAtBefore(
            NewsletterFrequency frequency,
            LocalDateTime time
    );
}
