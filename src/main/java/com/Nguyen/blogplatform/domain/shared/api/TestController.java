package com.Nguyen.blogplatform.domain.shared.api;



import com.Nguyen.blogplatform.domain.post.domain.model.Post;
import com.Nguyen.blogplatform.domain.post.infrastructure.repository.PostRepository;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
import com.Nguyen.blogplatform.shared.dto.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {
    private PostRepository postRepository;

    @GetMapping("/all")
    public String allAccess() {
        return "Public Content.";
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public String userAccess() {
        return "User Content.";
    }

    @GetMapping("/author")
    @PreAuthorize("hasRole('AUTHOR')")
    public String moderatorAccess() {
        return "AUTHOR Board.";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess() {
        return "Admin Board.";
    }

    @GetMapping
   public ResponseEntity<?> test(){
        return ResponseEntity.ok(new MessageResponse("Hello World!"));
   }
  @GetMapping("/posts")
   public ResponseEntity<List<Post>> testString(){
        return ResponseEntity.ok(postRepository.findAll());
   }
}