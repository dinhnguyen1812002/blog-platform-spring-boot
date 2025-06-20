package com.Nguyen.blogplatform.controller;

import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.payload.response.MessageResponse;
import com.Nguyen.blogplatform.repository.PostRepository;
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