package com.Nguyen.blogplatform.mapper;


import com.Nguyen.blogplatform.model.Comment;
import com.Nguyen.blogplatform.payload.response.CommentResponse;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {
    public CommentResponse toCommentResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUser().getUsername()
        );
    }
}

