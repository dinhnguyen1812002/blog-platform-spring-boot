package com.Nguyen.blogplatform.service.notification;

import com.Nguyen.blogplatform.Enum.EDeliveryChannel;
import com.Nguyen.blogplatform.Enum.EDeliveryStatus;
import com.Nguyen.blogplatform.model.NotificationHistory;
import com.Nguyen.blogplatform.model.Notifications;
import com.Nguyen.blogplatform.model.UserNotificationPreferences;
import com.Nguyen.blogplatform.payload.request.notification.UpdatePreferencesRequest;
import com.Nguyen.blogplatform.payload.response.CommentResponse;
import com.Nguyen.blogplatform.payload.response.notification.NotificationHistoryResponse;
import com.Nguyen.blogplatform.payload.response.notification.PublicArticleNotification;
import com.Nguyen.blogplatform.repository.NotificationHistoryRepository;
import com.Nguyen.blogplatform.repository.NotificationRepository;
import com.Nguyen.blogplatform.repository.UserNotificationPreferencesRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final NotificationHistoryRepository historyRepository;
    private final UserNotificationPreferencesRepository preferencesRepository;
    private final UserRepository userRepository;

    public NotificationService(SimpMessagingTemplate messagingTemplate,
                               NotificationRepository notificationRepository,
                               NotificationHistoryRepository historyRepository,
                               UserNotificationPreferencesRepository preferencesRepository,
                               UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.notificationRepository = notificationRepository;
        this.historyRepository = historyRepository;
        this.preferencesRepository = preferencesRepository;
        this.userRepository = userRepository;
    }

    public List<Notifications> getNotificationsOfUser(String userId) {
        return notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(ir -> Boolean.FALSE.equals(ir.getIsRead()))
                .toList();
    }

    public void sendPostNotification(String postId, String message) {
        messagingTemplate.convertAndSend("/topic/post/" + postId, message);
    }

    public void sendCommentNotification(String postId, CommentResponse comment) {
        messagingTemplate.convertAndSend("/topic/comments/" + postId, comment);
    }

    /**
     * Broadcast article published notification to all connected users
     * This is used for real-time updates when an article is published
     */
    public void broadcastArticlePublishedNotification(PublicArticleNotification notify) {
        try {
            log.info("Broadcasting article published notification: {}", notify.postId());
            messagingTemplate.convertAndSend("/topic/articles/published", notify);
        } catch (Exception e) {
            log.error("Error broadcasting article published notification", e);
        }
    }

    public void sendGlobalNotification(String message) {
        messagingTemplate.convertAndSend("/topic/global", message);
    }

    /**
     * Send notification to specific user about their published post
     */
    public void sendPostPublishedNotification(String username, PublicArticleNotification notify) {
        try {
            log.info("Sending post published notification to user: {}", username);
            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/post/published",
                    notify
            );
        } catch (Exception e) {
            log.error("Error sending post published notification to user: {}", username, e);
        }
    }


    public Notifications createUserNotification(
            String userId,
            String type,
            String title,
            String message
    ) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notifications n = new Notifications();
        n.setUser(user);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setIsRead(false);
        n.setCreatedAt(LocalDateTime.now());

        Notifications saved = notificationRepository.save(n);

        messagingTemplate.convertAndSendToUser(
                user.getUsername(),
                "/queue/notifications",
                saved
        );

        return saved;
    }

    public Notifications getNotificationById(String id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
    }

    /**
     * Send notification to post author when someone comments on their post
     */
    public void notifyPostAuthorAboutComment(String postAuthorId, String commenterUsername, String postTitle, String postId) {
        try {
            String title = "New Comment on Your Post";
            String message = String.format("%s has commented on your post: %s", 
                commenterUsername, postTitle);
            
            createUserNotification(
                postAuthorId,
                "NEW_COMMENT",
                title,
                message
            );
            
            log.info("Notification sent to post author {} about new comment from {}", postAuthorId, commenterUsername);
        } catch (Exception e) {
            log.error("Error sending comment notification to post author", e);
        }
    }

    // New methods for NotificationController

    public void sendBulkNotifications(List<String> userIds, String templateCode, Map<String, Object> templateData, List<EDeliveryChannel> channels) {
        log.info("Sending bulk notifications to {} users for template {}", userIds.size(), templateCode);
        // Implement logic or queue it
    }

    public void sendNotificationAsync(String userId, String templateCode, Map<String, Object> templateData, List<EDeliveryChannel> channels) {
        log.info("Sending async notification to user {} for template {}", userId, templateCode);
        // Implement logic or queue it
    }

    public Page<NotificationHistoryResponse> getUserNotifications(String userId, Pageable pageable) {
        return historyRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToHistoryResponse);
    }

    public Long getUnreadCount(String userId) {
        return notificationRepository.countByUser_IdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(String id, String userId) {
        Notifications notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (notification.getUser().getId().equals(userId)) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllAsRead(String userId) {
        List<Notifications> unread = notificationRepository.findByUser_IdAndIsReadFalse(userId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void updatePreferences(String userId, UpdatePreferencesRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getPreferences() != null) {
            for (var prefRequest : request.getPreferences()) {
                UserNotificationPreferences prefs = preferencesRepository.findByUserIdAndChannel(userId, prefRequest.getChannel())
                        .orElse(UserNotificationPreferences.builder()
                                .user(user)
                                .channel(prefRequest.getChannel())
                                .build());

                if (prefRequest.getEnabled() != null) prefs.setEnabled(prefRequest.getEnabled());
                if (prefRequest.getDigestMode() != null) prefs.setDigestMode(prefRequest.getDigestMode());
                if (prefRequest.getQuietHoursStart() != null) prefs.setQuietHoursStart(prefRequest.getQuietHoursStart());
                if (prefRequest.getQuietHoursEnd() != null) prefs.setQuietHoursEnd(prefRequest.getQuietHoursEnd());
                if (prefRequest.getEmailAddress() != null) prefs.setEmailAddress(prefRequest.getEmailAddress());
                if (prefRequest.getPushToken() != null) prefs.setPushToken(prefRequest.getPushToken());
                if (prefRequest.getDeviceType() != null) prefs.setDeviceType(prefRequest.getDeviceType());

                preferencesRepository.save(prefs);
            }
        }
    }

    public List<UserNotificationPreferences> getUserPreferences(String userId) {
        return preferencesRepository.findByUserId(userId);
    }

    private NotificationHistoryResponse mapToHistoryResponse(NotificationHistory history) {
        return NotificationHistoryResponse.builder()
                .id(history.getId())
                .notificationId(history.getNotificationId())
                .channel(history.getChannel())
                .status(history.getStatus())
                .subject(history.getSubject())
                .content(history.getContent())
                .createdAt(history.getCreatedAt())
                .sentAt(history.getSentAt())
                .readAt(history.getReadAt())
                .build();
    }
}
