package com.Nguyen.blogplatform.payload.response.notification;

import java.time.LocalDateTime;

public record PublicArticleNotification(
       String postId,
       String title,
       String excerpt,
       String slug,
       LocalDateTime public_date)
{
}
