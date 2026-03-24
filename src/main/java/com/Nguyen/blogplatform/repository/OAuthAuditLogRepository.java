package com.Nguyen.blogplatform.repository;

import com.Nguyen.blogplatform.Enum.EOAuthProvider;
import com.Nguyen.blogplatform.model.OAuthAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OAuthAuditLogRepository extends JpaRepository<OAuthAuditLog, String> {

    List<OAuthAuditLog> findByUserIdOrderByCreatedAtDesc(String userId);

    Page<OAuthAuditLog> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    List<OAuthAuditLog> findByEventTypeOrderByCreatedAtDesc(String eventType);

    List<OAuthAuditLog> findByProviderAndCreatedAtBetweenOrderByCreatedAtDesc(
        EOAuthProvider provider, LocalDateTime start, LocalDateTime end);

    @Query("SELECT oal FROM OAuthAuditLog oal WHERE oal.ipAddress = :ipAddress AND oal.createdAt > :since ORDER BY oal.createdAt DESC")
    List<OAuthAuditLog> findRecentByIpAddress(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(oal) FROM OAuthAuditLog oal WHERE oal.userId = :userId AND oal.eventType = :eventType AND oal.createdAt > :since")
    long countRecentEventsByUserAndType(@Param("userId") String userId, @Param("eventType") String eventType, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(oal) FROM OAuthAuditLog oal WHERE oal.ipAddress = :ipAddress AND oal.success = false AND oal.createdAt > :since")
    long countFailedAttemptsByIp(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);
}
