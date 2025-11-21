package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.model.Notifications;
import com.Nguyen.blogplatform.payload.response.CommentResponse;
import com.Nguyen.blogplatform.payload.response.notification.PublicArticleNotification;
import com.Nguyen.blogplatform.repository.NotificationRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(SimpMessagingTemplate messagingTemplate,
                               NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public List<Notifications> getNotificationsOfUser(String userId) {
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId);
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
//    public List<Notifications> getNotificationsOfUser(String userId) {
//        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
//    }


    public Notifications getNotificationById(String id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
    }

}
