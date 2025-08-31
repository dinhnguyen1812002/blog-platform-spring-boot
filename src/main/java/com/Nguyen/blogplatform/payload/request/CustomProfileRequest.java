package com.Nguyen.blogplatform.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomProfileRequest {
    
    @NotBlank(message = "Markdown content cannot be blank")
    private String markdownContent;
}