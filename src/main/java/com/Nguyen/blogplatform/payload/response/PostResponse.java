package com.Nguyen.blogplatform.payload.response;

import com.Nguyen.blogplatform.Enum.PublishStatus;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Full response object for a post, including content and comments.
 */
public record PostResponse(
        String id,
        String title,
        String slug,
        String excerpt,
        String content,
        String thumbnail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime publishedAt,
        Boolean featured,
        PublishStatus visibility,
        LocalDateTime scheduledPublishAt,
        Long viewCount,
        Long likeCount,
        Double averageRating,
        UserResponse author,
        Set<CategoryResponse> categories,
        Set<TagResponse> tags,
        Boolean isLikedByCurrentUser,
        Boolean isSavedByCurrentUser,
        Integer userRating,
        Integer commentCount,
        List<CommentResponse> comments
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
