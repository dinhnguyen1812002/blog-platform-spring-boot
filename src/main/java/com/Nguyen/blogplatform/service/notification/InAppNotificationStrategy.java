package com.Nguyen.blogplatform.service.notification;

import com.Nguyen.blogplatform.Enum.EDeliveryChannel;
import com.Nguyen.blogplatform.Enum.EDeliveryStatus;
import com.Nguyen.blogplatform.model.NotificationHistory;
import com.Nguyen.blogplatform.model.NotificationTemplate;
import com.Nguyen.blogplatform.model.Notifications;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.repository.NotificationRepository;
import com.Nguyen.blogplatform.repository.UserNotificationPreferencesRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class InAppNotificationStrategy implements NotificationStrategy {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final UserNotificationPreferencesRepository preferencesRepository;
    private final TemplateEngine templateEngine;

    @Override
    public String getChannel() {
        return EDeliveryChannel.IN_APP.name();
    }

    @Override
    public NotificationHistory send(NotificationHistory history, NotificationTemplate template, Map<String, Object> templateData) {
        try {
            User user = userRepository.findById(history.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + history.getUserId()));

            String processedContent = processTemplate(template.getContent(), templateData);
            String processedTitle = processTemplate(template.getTitle(), templateData);

            Notifications notification = new Notifications();
            notification.setUser(user);
            notification.setType(template.getType());
            notification.setTitle(processedTitle);
            notification.setMessage(processedContent);
            notification.setIsRead(false);
            notification.setCreatedAt(LocalDateTime.now());

            Notifications saved = notificationRepository.save(notification);

            messagingTemplate.convertAndSendToUser(
                user.getUsername(),
                "/queue/notifications",
                saved
            );

            history.setStatus(EDeliveryStatus.DELIVERED);
            history.setDeliveredAt(LocalDateTime.now());
            history.setContent(processedContent);

            log.info("In-app notification sent to user: {}", history.getUserId());

        } catch (Exception e) {
            log.error("Failed to send in-app notification", e);
            history.setStatus(EDeliveryStatus.FAILED);
            history.setErrorMessage(e.getMessage());
        }

        return history;
    }

    @Override
    public boolean isAvailable(String userId) {
        return preferencesRepository.findByUserIdAndChannel(userId, EDeliveryChannel.IN_APP)
            .map(pref -> pref.getEnabled() != null && pref.getEnabled())
            .orElse(true);
    }

    private String processTemplate(String template, Map<String, Object> data) {
        if (template == null || data == null || data.isEmpty()) {
            return template;
        }

        try {
            Context context = new Context();
            context.setVariables(data);
            return templateEngine.process(template, context);
        } catch (Exception e) {
            log.warn("Template processing failed, returning raw template", e);
            return template;
        }
    }
}
