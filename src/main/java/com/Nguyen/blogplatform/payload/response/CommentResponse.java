package com.Nguyen.blogplatform.payload.response;

import com.Nguyen.blogplatform.payload.request.CommentRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@Data
@NoArgsConstructor

public class CommentResponse {
    private String id;
    private String content;
    private UserResponse user;
    private Instant createdAt;
    private Instant updatedAt;
    private int depth;
    private String parentCommentId;
    private int replyCount;
    private List<CommentResponse> replies;
    private boolean hasMoreReplies;


    public CommentResponse(String id,
                           @NotEmpty(message = "*Please provide comment content")
                           @Size(min = 1, message = "*Your comment must not be empty")
                           String content,
                           Instant createdAt,

                           UserResponse user) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.user = user;
    }
}
