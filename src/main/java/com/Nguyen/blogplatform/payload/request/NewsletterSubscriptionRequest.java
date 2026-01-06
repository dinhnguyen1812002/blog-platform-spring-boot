package com.Nguyen.blogplatform.payload.request;

import com.Nguyen.blogplatform.Enum.NewsletterFrequency;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewsletterSubscriptionRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;
    
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    private NewsletterFrequency frequency;
}
