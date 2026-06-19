package com.Nguyen.blogplatform.domain.newsletter.dto;



import com.Nguyen.blogplatform.shared.enums.ENewsletterStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriberResponse {

    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private ENewsletterStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime unsubscribedAt;
    private LocalDateTime lastSentAt;
    private String tags;
}
