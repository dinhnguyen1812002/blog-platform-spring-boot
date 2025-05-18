package com.Nguyen.blogplatform.payload.response;

import com.Nguyen.blogplatform.model.User;
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
    private String title;
    private String slug;
    private String content;
    private String imageUrl;
    private Date createdAt;
    private Boolean featured;
    private UserResponse user;
    private Set<String> categories;
    private List<CommentResponse> comments; // Thêm trường này


    // Constructor without comments and featured

    public PostResponse(String id, UserResponse user,
                        String title, String slug, Date createdAt,
                        Boolean featured, String content, String imageUrl,
                        Set<String> categories) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.slug = slug;
        this.createdAt = createdAt;
        this.featured = featured;
        this.content = content;
        this.imageUrl = imageUrl;
        this.categories = categories;
    }

    // Constructor with comments, without featured

    // Constructor with all fields
    public PostResponse(String id,  UserResponse user,
                        String title, String slug, Date createdAt,
                        Boolean featured, String content,
                        String imageUrl, Set<String> categories,
                        List<CommentResponse> comments) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.slug = slug;
        this.createdAt = createdAt;
        this.featured = featured;
        this.content = content;
        this.imageUrl = imageUrl;
        this.categories = categories;
        this.comments = comments;
    }

    public <R> PostResponse(String id,
                            UserResponse userResponse,
                            @Size(min = 5,
                                    message = "*Your title must have at least 5 characters")
                            @NotEmpty(message = "*Please provide a title")
                            String title, @Size(min = 5, message = "*Your title must have at least 5 characters")
                            @NotEmpty(message = "*Please provide a title") String slug,
                            Date createdAt,
                            String content,
                            String imageUrl,
                            R collect) {
    }
}
