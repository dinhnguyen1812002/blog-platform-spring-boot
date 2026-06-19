package com.Nguyen.blogplatform.domain.notification.dto;



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
