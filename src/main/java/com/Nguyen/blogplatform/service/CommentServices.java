package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.model.Comment;
import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.request.CommentRequest;
import com.Nguyen.blogplatform.payload.response.CommentResponse;
import com.Nguyen.blogplatform.repository.CommentRepository;
import com.Nguyen.blogplatform.repository.PostRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;


@Service
public class CommentServices {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    public CommentResponse createComment(CommentRequest commentRequest, String postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        Comment comment = new Comment();
        comment.setComment(commentRequest.getComment());
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setCreatedAt(new Date());

        Comment savedComment = commentRepository.save(comment);

        return new CommentResponse(
                savedComment.getId(),
                savedComment.getComment(),
                savedComment.getCreatedAt(),
                author.getUsername()
        );
    }

}
