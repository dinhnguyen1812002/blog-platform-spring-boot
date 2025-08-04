package com.Nguyen.blogplatform.payload.response;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
    private String id;
    private UserResponse user;
    @NotEmpty(message = "*Please provide a title")
    @Size(min = 5, message = "*Your title must have at least 5 characters")
    private String title;
    @NotEmpty(message = "*Please provide a slug")
    @Size(min = 5, message = "*Your slug must have at least 5 characters")
    private String slug;
    private Date createdAt;
    private Boolean featured;
    private String content;
    private String thumbnail;
    private Set<CategoryResponse> categories;
    private Set<TagResponse> tags;
    private Integer commentCount;
    private Long viewCount;
    private Long likeCount;
    private Double averageRating;
    private Boolean isLikedByCurrentUser;
    private Boolean isSavedByCurrentUser;
    private Integer userRating;
    private List<CommentResponse> comments;
}