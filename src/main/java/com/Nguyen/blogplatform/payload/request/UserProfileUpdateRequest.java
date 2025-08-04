package com.Nguyen.blogplatform.payload.request;



import com.Nguyen.blogplatform.Enum.ESocialMediaPlatform;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class UserProfileUpdateRequest {
    @Size(min = 3, message = "Username must have at least 3 characters")
    private String username;

    @Email(message = "Please provide a valid email")
    private String email;

    private String avatar;
    private String bio;
    private String website;
    private Map<ESocialMediaPlatform, String> socialMediaLinks; // Map platform -> url
}