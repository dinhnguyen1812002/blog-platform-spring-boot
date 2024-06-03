package com.Nguyen.blogplatform.payload.request;



import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {

    @NotEmpty(message = "*Please provide content for the comment")
    @Size(min = 1, message = "*The comment must have at least 1 character")
    private String comment;


}
