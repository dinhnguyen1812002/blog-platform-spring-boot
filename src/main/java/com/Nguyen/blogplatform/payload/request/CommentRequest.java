package com.Nguyen.blogplatform.payload.request;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class CommentRequest {
    @NotBlank
    private String content;
    private String parentCommentId;
}