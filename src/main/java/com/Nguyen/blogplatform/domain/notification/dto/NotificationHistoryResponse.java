package com.Nguyen.blogplatform.domain.notification.dto;



import com.Nguyen.blogplatform.shared.enums.EDeliveryChannel;
import com.Nguyen.blogplatform.shared.enums.EDeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationHistoryResponse {

    private String id;
    private String notificationId;
    private EDeliveryChannel channel;
    private EDeliveryStatus status;
    private String subject;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
}
