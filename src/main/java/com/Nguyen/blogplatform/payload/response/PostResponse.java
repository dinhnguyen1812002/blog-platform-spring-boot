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
@AllArgsConstructor
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

    public PostResponse(String id, String title,
                        String content, Date createdAt,
                        String imageUrl, String username,
                        List<CommentResponse> commentResponses) {

        this.id = id;
        this.authorName = username;
        this.title = title;
        this.createdAt = createdAt;
        this.content = content;
        this.imageUrl = imageUrl;
        this.comments= commentResponses;
    }
}
