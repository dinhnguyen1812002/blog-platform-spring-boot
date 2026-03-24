package com.Nguyen.blogplatform.payload.response.newsletter;

import com.Nguyen.blogplatform.Enum.ECampaignStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignResponse {

    private String id;
    private String name;
    private String subject;
    private ECampaignStatus status;
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private Long recipientCount;
    private Long sentCount;
    private Long openedCount;
    private Long clickedCount;
    private Long bouncedCount;
    private Long unsubscribedCount;
    private LocalDateTime createdAt;
}
