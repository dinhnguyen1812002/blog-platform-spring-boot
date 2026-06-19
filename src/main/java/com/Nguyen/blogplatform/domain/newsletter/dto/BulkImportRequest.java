package com.Nguyen.blogplatform.domain.newsletter.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkImportRequest {

    private List<SubscriberImportRow> subscribers;

    private boolean requireConfirmation;

    private boolean sendWelcomeEmail;
}
