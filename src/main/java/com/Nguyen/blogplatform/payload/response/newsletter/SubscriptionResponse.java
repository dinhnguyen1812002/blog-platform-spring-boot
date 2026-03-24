package com.Nguyen.blogplatform.payload.response.newsletter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {

    private boolean success;
    private String message;
    private boolean requiresConfirmation;
}
