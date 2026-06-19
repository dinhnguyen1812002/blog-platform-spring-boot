package com.Nguyen.blogplatform.domain.post.domain.model;



import com.Nguyen.blogplatform.domain.user.domain.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "rating")
@Getter
@Setter
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "score", nullable = false)
    private Integer score; // 1 to 5

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Rating() {
        this.createdAt = LocalDateTime.now();
    }

    public Rating(Integer score, Post post, User user) {
        this.score = score;
        this.post = post;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }
}