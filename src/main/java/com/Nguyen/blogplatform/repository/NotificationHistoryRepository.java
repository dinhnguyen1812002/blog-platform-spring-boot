package com.Nguyen.blogplatform.repository;

import com.Nguyen.blogplatform.Enum.EDeliveryChannel;
import com.Nguyen.blogplatform.Enum.EDeliveryStatus;
import com.Nguyen.blogplatform.model.NotificationHistory;
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
public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, String> {

    List<NotificationHistory> findByUserIdOrderByCreatedAtDesc(String userId);

    Page<NotificationHistory> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    List<NotificationHistory> findByNotificationId(String notificationId);

    List<NotificationHistory> findByStatusAndNextRetryAtBefore(EDeliveryStatus status, LocalDateTime time);

    List<NotificationHistory> findByUserIdAndStatus(String userId, EDeliveryStatus status);

    Page<NotificationHistory> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, EDeliveryStatus status, Pageable pageable);

    List<NotificationHistory> findByChannelAndStatus(EDeliveryChannel channel, EDeliveryStatus status);

    long countByUserIdAndStatusAndReadAtIsNull(String userId, EDeliveryStatus status);

    Optional<NotificationHistory> findByIdAndUserId(String id, String userId);

    @Modifying
    @Query("UPDATE NotificationHistory nh SET nh.status = :status, nh.readAt = :readAt WHERE nh.id = :id")
    int updateStatusAndReadAt(@Param("id") String id, @Param("status") EDeliveryStatus status, @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("UPDATE NotificationHistory nh SET nh.status = :status, nh.sentAt = :sentAt WHERE nh.id = :id")
    int updateStatusAndSentAt(@Param("id") String id, @Param("status") EDeliveryStatus status, @Param("sentAt") LocalDateTime sentAt);

    @Query("SELECT nh FROM NotificationHistory nh WHERE nh.userId = :userId AND nh.status = 'DELIVERED' AND nh.readAt IS NULL ORDER BY nh.createdAt DESC")
    List<NotificationHistory> findUnreadByUserId(@Param("userId") String userId);

    @Query("SELECT COUNT(nh) FROM NotificationHistory nh WHERE nh.userId = :userId AND nh.channel = :channel AND nh.status = :status AND nh.createdAt > :since")
    long countRecentByUserAndChannel(@Param("userId") String userId, @Param("channel") EDeliveryChannel channel, @Param("status") EDeliveryStatus status, @Param("since") LocalDateTime since);
}
