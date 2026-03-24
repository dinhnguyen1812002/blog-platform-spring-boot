package com.Nguyen.blogplatform.payload.request;

import com.Nguyen.blogplatform.Enum.PublishStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Request object for creating or updating a post.
 */
public record PostRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
        String title,

        @Size(min = 5, message = "Excerpt must be at least 5 characters")
        String excerpt,

        @NotBlank(message = "Content is required")
        @Size(min = 10, message = "Content must be at least 10 characters")
        String content,

        String thumbnail,

        @NotNull(message = "Categories are required")
        Set<Long> categories,

        Set<UUID> tags,

        Boolean featured,

        PublishStatus visibility,

        LocalDateTime scheduledPublishAt,

        LocalDateTime publishedAt
) {}
