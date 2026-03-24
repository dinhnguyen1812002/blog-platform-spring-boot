package com.Nguyen.blogplatform.repository;

import com.Nguyen.blogplatform.model.EmailLog;
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
public interface EmailLogRepository extends JpaRepository<EmailLog, String> {

    List<EmailLog> findByCampaignId(String campaignId);

    Page<EmailLog> findByCampaignId(String campaignId, Pageable pageable);

    List<EmailLog> findBySubscriberId(String subscriberId);

    Optional<EmailLog> findByExternalMessageId(String externalMessageId);

    List<EmailLog> findByStatusAndCreatedAtBefore(EmailLog.EmailStatus status, LocalDateTime time);

    @Query("SELECT COUNT(el) FROM EmailLog el WHERE el.campaignId = :campaignId AND el.status = :status")
    long countByCampaignAndStatus(@Param("campaignId") String campaignId, @Param("status") EmailLog.EmailStatus status);

    @Modifying
    @Query("UPDATE EmailLog el SET el.status = 'OPENED', el.openedAt = :now WHERE el.externalMessageId = :messageId")
    int markAsOpened(@Param("messageId") String messageId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE EmailLog el SET el.status = 'CLICKED', el.clickedAt = :now WHERE el.externalMessageId = :messageId")
    int markAsClicked(@Param("messageId") String messageId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE EmailLog el SET el.status = 'BOUNCED', el.bounceReason = :reason, el.bounceType = :bounceType WHERE el.externalMessageId = :messageId")
    int markAsBounced(@Param("messageId") String messageId, @Param("reason") String reason, @Param("bounceType") String bounceType);

    @Modifying
    @Query("UPDATE EmailLog el SET el.status = 'COMPLAINED', el.complaintType = :complaintType WHERE el.externalMessageId = :messageId")
    int markAsComplained(@Param("messageId") String messageId, @Param("complaintType") String complaintType);

    @Query("SELECT el FROM EmailLog el WHERE el.campaignId = :campaignId AND el.status IN ('PENDING', 'RETRYING') ORDER BY el.createdAt ASC")
    List<EmailLog> findPendingByCampaign(@Param("campaignId") String campaignId);

    @Query("SELECT COUNT(el) FROM EmailLog el WHERE el.subscriberId = :subscriberId AND el.status = 'BOUNCED' AND el.createdAt > :since")
    long countRecentBouncesBySubscriber(@Param("subscriberId") String subscriberId, @Param("since") LocalDateTime since);
}
