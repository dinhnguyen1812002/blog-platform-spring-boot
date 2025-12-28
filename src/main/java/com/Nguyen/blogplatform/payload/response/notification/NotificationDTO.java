package com.Nguyen.blogplatform.payload.response.notification;

import java.time.LocalDateTime;

public record NotificationDTO(
        String notificationId,
        String type,
        String title,
        String message,
        Boolean isRead,
        LocalDateTime createdAt
) {}
