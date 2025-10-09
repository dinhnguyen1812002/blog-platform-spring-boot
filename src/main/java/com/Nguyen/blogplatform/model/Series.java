package com.Nguyen.blogplatform.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entity đại diện cho một Series (chuỗi bài viết)
 * Một series chứa nhiều bài viết được sắp xếp theo thứ tự
 */
@Entity
@Table(name = "series")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Series {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "title", nullable = false)
    @Size(min = 5, max = 200, message = "Series title must be between 5 and 200 characters")
    @NotEmpty(message = "Please provide a series title")
    private String title;

    @Column(name = "slug", nullable = false, unique = true)
    @Size(min = 5, max = 250, message = "Series slug must be between 5 and 250 characters")
    @NotEmpty(message = "Please provide a series slug")
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    @Size(min = 10, message = "Description must have at least 10 characters")
    private String description;

    @Column(name = "thumbnail")
    private String thumbnail;

    // Tác giả của series
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    // Danh sách các bài viết trong series với thứ tự
    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("series-posts")
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<SeriesPost> seriesPosts = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "total_posts")
    @Builder.Default
    private Integer totalPosts = 0;

    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods để quản lý posts trong series
    public void addPost(Post post, Integer orderIndex) {
        SeriesPost seriesPost = new SeriesPost();
        seriesPost.setSeries(this);
        seriesPost.setPost(post);
        seriesPost.setOrderIndex(orderIndex);
        seriesPosts.add(seriesPost);
        this.totalPosts = seriesPosts.size();
    }

    public void removePost(Post post) {
        seriesPosts.removeIf(sp -> sp.getPost().getId().equals(post.getId()));
        this.totalPosts = seriesPosts.size();
        reorderPosts();
    }

    // Sắp xếp lại thứ tự các bài viết sau khi xóa
    private void reorderPosts() {
        for (int i = 0; i < seriesPosts.size(); i++) {
            seriesPosts.get(i).setOrderIndex(i + 1);
        }
    }

    public void incrementViewCount() {
        this.viewCount++;
    }
}