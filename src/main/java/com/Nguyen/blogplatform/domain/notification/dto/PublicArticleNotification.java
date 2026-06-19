package com.Nguyen.blogplatform.domain.notification.dto;



import java.time.LocalDateTime;

public record PublicArticleNotification(
       String postId,
       String title,
       String thumbnail,
       String excerpt,
       String slug,
       LocalDateTime public_date)
{
}
