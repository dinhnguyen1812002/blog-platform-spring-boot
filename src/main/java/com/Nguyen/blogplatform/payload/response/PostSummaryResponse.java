package com.Nguyen.blogplatform.payload.response;

import com.Nguyen.blogplatform.Enum.PublishStatus;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Summary response object for a post, suitable for lists.
 */
public record PostSummaryResponse(
        String id,
        String title,
        String slug,
        String excerpt,
        String thumbnail,
        LocalDateTime createdAt,
        LocalDateTime publishedAt,
        Boolean featured,
        PublishStatus visibility,
        Long viewCount,
        Long likeCount,
        Double averageRating,
        UserResponse author,
        Set<CategoryResponse> categories,
        Set<TagResponse> tags,
        Integer commentCount,
        Boolean isLikedByCurrentUser,
        Boolean isSavedByCurrentUser
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
