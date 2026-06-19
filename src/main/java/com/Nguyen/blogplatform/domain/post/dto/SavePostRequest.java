package com.Nguyen.blogplatform.domain.post.dto;



import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SavePostRequest {
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
