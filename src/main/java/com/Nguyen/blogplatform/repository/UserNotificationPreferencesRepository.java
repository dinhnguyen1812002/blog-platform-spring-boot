package com.Nguyen.blogplatform.repository;

import com.Nguyen.blogplatform.Enum.EDeliveryChannel;
import com.Nguyen.blogplatform.model.UserNotificationPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationPreferencesRepository extends JpaRepository<UserNotificationPreferences, String> {

    Optional<UserNotificationPreferences> findByUserIdAndChannel(String userId, EDeliveryChannel channel);

    List<UserNotificationPreferences> findByUserId(String userId);

    List<UserNotificationPreferences> findByChannelAndEnabledTrue(EDeliveryChannel channel);

    List<UserNotificationPreferences> findByUserIdAndEnabledTrue(String userId);

    boolean existsByUserIdAndChannel(String userId, EDeliveryChannel channel);

    @Query("SELECT COUNT(unp) FROM UserNotificationPreferences unp WHERE unp.user.id = :userId AND unp.enabled = true")
    long countEnabledByUserId(@Param("userId") String userId);

    @Query("SELECT unp FROM UserNotificationPreferences unp WHERE unp.user.id = :userId AND unp.channel = :channel AND unp.enabled = true")
    Optional<UserNotificationPreferences> findEnabledByUserAndChannel(@Param("userId") String userId, @Param("channel") EDeliveryChannel channel);
}
