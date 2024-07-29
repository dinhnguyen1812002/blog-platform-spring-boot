package com.Nguyen.blogplatform.payload.response;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class PostResponse {
    private String id;
    private String authorName;
    private String title;
    private Date createdAt;
    private Boolean featured;
    private String content;
    private String imageUrl;
    private Set<String> categories;
    private List<CommentResponse> comments;

    // Constructor without comments and featured
    public PostResponse(String id, String authorName,
                        String title, Date createdAt,
                        String content, String imageUrl,
                        Set<String> categories) {
        this.id = id;
        this.authorName = authorName;
        this.title = title;
        this.createdAt = createdAt;
        this.content = content;
        this.imageUrl = imageUrl;
        this.categories = categories;
    }

    // Constructor with comments, without featured
    public PostResponse(String id, String authorName,
                        String title, Date createdAt,
                        String content, String imageUrl,
                        Set<String> categories, List<CommentResponse> comments) {
        this.id = id;
        this.authorName = authorName;
        this.title = title;
        this.createdAt = createdAt;
        this.content = content;
        this.imageUrl = imageUrl;
        this.categories = categories;
        this.comments = comments;
    }

    // Constructor with all fields
    public PostResponse(String id, String authorName,
                        String title, Date createdAt,
                        Boolean featured, String content,
                        String imageUrl, Set<String> categories,
                        List<CommentResponse> comments) {
        this.id = id;
        this.authorName = authorName;
        this.title = title;
        this.createdAt = createdAt;
        this.featured = featured;
        this.content = content;
        this.imageUrl = imageUrl;
        this.categories = categories;
        this.comments = comments;
    }
}
