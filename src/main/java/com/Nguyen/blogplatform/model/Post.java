package com.Nguyen.blogplatform.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.info.Info;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "post")
@Getter
@Setter
@AllArgsConstructor
@Builder
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

    @Column(name = "thumbnail")
    private String thumbnail;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;
    @Column(name = "featured", nullable = false)
    private Boolean featured;
    @Column(name = "view", nullable = false)
    @Builder.Default
    private Long view = 0L;

    @ManyToMany
    @JoinTable(
            name = "post_like",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> like = new HashSet<>();

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
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Builder.Default
    @JsonManagedReference("post-comments")
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonManagedReference("post-ratings")
    @Builder.Default
    private Set<Rating> ratings = new HashSet<>();
    @ManyToMany
    @JoinTable(
            name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tags> tags = new HashSet<>();
    public Post() {

        this.createdAt = new Date();

        this.view= 0L;
    }

    public Post(String title, String slug, String content, String thumbnail, User user) {
        this.title = title;
        this.slug = slug;
        this.content = content;
        this.thumbnail = thumbnail;
        this.createdAt = new Date();
        this.user = user;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Chỉ dùng ID
    }
}
