package com.Nguyen.blogplatform.repository;

import com.Nguyen.blogplatform.Enum.ENewsletterStatus;
import com.Nguyen.blogplatform.model.NewsletterSubscriber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, String> {

    Optional<NewsletterSubscriber> findByEmail(String email);

    Optional<NewsletterSubscriber> findByConfirmationToken(String token);

    Optional<NewsletterSubscriber> findByUnsubscribeToken(String token);

    boolean existsByEmail(String email);

    Page<NewsletterSubscriber> findByStatus(ENewsletterStatus status, Pageable pageable);

    Page<NewsletterSubscriber> findByStatusIn(List<ENewsletterStatus> statuses, Pageable pageable);

    List<NewsletterSubscriber> findByStatusAndConfirmationTokenExpiresAtBefore(ENewsletterStatus status, LocalDateTime time);

    @Query("SELECT ns FROM NewsletterSubscriber ns WHERE ns.status = 'ACTIVE' AND (ns.tags LIKE %:tag% OR :tag IS NULL)")
    List<NewsletterSubscriber> findActiveByTag(@Param("tag") String tag);

    @Query("SELECT COUNT(ns) FROM NewsletterSubscriber ns WHERE ns.status = 'ACTIVE'")
    long countActiveSubscribers();

    @Query("SELECT ns FROM NewsletterSubscriber ns WHERE ns.status = 'ACTIVE' AND ns.lastSentAt < :since OR ns.lastSentAt IS NULL")
    List<NewsletterSubscriber> findActiveNotSentSince(@Param("since") LocalDateTime since);

    @Modifying
    @Query("UPDATE NewsletterSubscriber ns SET ns.status = 'UNSUBSCRIBED', ns.unsubscribedAt = :now WHERE ns.id = :id")
    int unsubscribe(@Param("id") String id, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE NewsletterSubscriber ns SET ns.status = 'ACTIVE', ns.confirmedAt = :now, ns.confirmationToken = NULL WHERE ns.id = :id")
    int confirmSubscription(@Param("id") String id, @Param("now") LocalDateTime now);

    @Query("SELECT ns FROM NewsletterSubscriber ns WHERE ns.status = 'PENDING' AND ns.confirmationTokenExpiresAt < :now")
    List<NewsletterSubscriber> findExpiredPendingSubscriptions(@Param("now") LocalDateTime now);

    Page<NewsletterSubscriber> findByEmailContainingIgnoreCase(String email, Pageable pageable);
}
