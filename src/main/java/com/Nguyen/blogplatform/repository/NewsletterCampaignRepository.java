package com.Nguyen.blogplatform.repository;

import com.Nguyen.blogplatform.Enum.ECampaignStatus;
import com.Nguyen.blogplatform.model.NewsletterCampaign;
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
public interface NewsletterCampaignRepository extends JpaRepository<NewsletterCampaign, String> {

    Page<NewsletterCampaign> findByStatus(ECampaignStatus status, Pageable pageable);

    Page<NewsletterCampaign> findByStatusIn(List<ECampaignStatus> statuses, Pageable pageable);

    List<NewsletterCampaign> findByStatusAndScheduledAtBefore(ECampaignStatus status, LocalDateTime time);

    @Query("SELECT nc FROM NewsletterCampaign nc WHERE nc.status IN ('SENDING', 'SCHEDULED') ORDER BY nc.scheduledAt ASC")
    List<NewsletterCampaign> findActiveOrScheduledCampaigns();

    @Modifying
    @Query("UPDATE NewsletterCampaign nc SET nc.status = :status WHERE nc.id = :id")
    int updateStatus(@Param("id") String id, @Param("status") ECampaignStatus status);

    @Modifying
    @Query("UPDATE NewsletterCampaign nc SET nc.sentCount = nc.sentCount + 1 WHERE nc.id = :id")
    int incrementSentCount(@Param("id") String id);

    @Modifying
    @Query("UPDATE NewsletterCampaign nc SET nc.openedCount = nc.openedCount + 1 WHERE nc.id = :id")
    int incrementOpenedCount(@Param("id") String id);

    @Modifying
    @Query("UPDATE NewsletterCampaign nc SET nc.clickedCount = nc.clickedCount + 1 WHERE nc.id = :id")
    int incrementClickedCount(@Param("id") String id);

    @Modifying
    @Query("UPDATE NewsletterCampaign nc SET nc.bouncedCount = nc.bouncedCount + 1 WHERE nc.id = :id")
    int incrementBouncedCount(@Param("id") String id);

    @Modifying
    @Query("UPDATE NewsletterCampaign nc SET nc.unsubscribedCount = nc.unsubscribedCount + 1 WHERE nc.id = :id")
    int incrementUnsubscribedCount(@Param("id") String id);

    @Query("SELECT nc FROM NewsletterCampaign nc WHERE nc.name LIKE %:search% OR nc.subject LIKE %:search%")
    Page<NewsletterCampaign> searchByNameOrSubject(@Param("search") String search, Pageable pageable);
}
