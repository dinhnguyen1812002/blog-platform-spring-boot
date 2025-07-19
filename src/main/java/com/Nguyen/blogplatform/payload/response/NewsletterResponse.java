package com.Nguyen.blogplatform.payload.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsletterResponse {
    private String id;
    private String email;
    private String name;
    private Boolean isActive;
    private Boolean isConfirmed;
    private LocalDateTime subscribedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime unsubscribedAt;
}
