package com.Nguyen.blogplatform.payload.request.newsletter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriberImportRow {

    @NotBlank
    @Email
    private String email;

    private String firstName;

    private String lastName;

    private String tags;

    private boolean gdprConsent;
}
