package com.Nguyen.blogplatform.payload.response.notification;

import com.Nguyen.blogplatform.Enum.EDeliveryChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private String id;
    private String type;
    private String title;
    private String message;
    private Boolean isRead;
    private String createdAt;
}
