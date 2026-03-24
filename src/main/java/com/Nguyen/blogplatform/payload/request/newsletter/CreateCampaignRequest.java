package com.Nguyen.blogplatform.payload.request.newsletter;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCampaignRequest {

    @NotBlank(message = "Campaign name is required")
    private String name;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "HTML content is required")
    private String htmlContent;

    private String textContent;

    private String fromName;

    private String fromEmail;

    private String replyTo;

    private LocalDateTime scheduledAt;

    private String targetSegment;

    private String targetTags;

    private Integer batchSize;

    private Integer sendIntervalSeconds;

    private String utmSource;

    private String utmMedium;

    private String utmCampaign;
}
