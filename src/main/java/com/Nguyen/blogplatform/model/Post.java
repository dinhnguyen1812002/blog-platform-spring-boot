package com.Nguyen.blogplatform.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "post")
@Getter
@Setter
@Data
public class Post {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column(name = "title", nullable = false)
    @Size(min = 5, message = "*Your title must have at least 5 characters")
    @NotEmpty(message = "*Please provide a title")
    private String title;
    @Size(min = 5, message = "*Your title must have at least 5 characters")
    @NotEmpty(message = "*Please provide a title")
    private String slug;
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;
    @Column(name = "featured", nullable = false)
    private Boolean featured;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @ManyToMany
    @JoinTable(
            name = "post_category",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @JsonManagedReference
    @NotEmpty(message = "A post must have at least one category")
    private Set<Category> categories = new HashSet<>();
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonManagedReference("post-comments")
    private Set<Comment> comments = new HashSet<>();
    public Post() {
        this.createdAt = new Date();
    }

    public Post(String title, String slug, String content, String imageUrl, User user) {
        this.title = title;
        this.slug = slug;
        this.content = content;
        this.imageUrl = imageUrl;
        this.createdAt = new Date();
        this.user = user;
    }

}
