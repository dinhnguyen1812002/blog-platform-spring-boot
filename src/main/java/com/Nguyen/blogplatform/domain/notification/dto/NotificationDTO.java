package com.Nguyen.blogplatform.domain.notification.dto;



import java.time.LocalDateTime;

public record NotificationDTO(
        String notificationId,
        String type,
        String title,
        String message,
        Boolean isRead,
        LocalDateTime createdAt
) {}
