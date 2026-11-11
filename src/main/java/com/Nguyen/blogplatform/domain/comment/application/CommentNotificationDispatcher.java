package com.Nguyen.blogplatform.domain.comment.application;

import com.Nguyen.blogplatform.domain.comment.dto.CommentResponse;
import com.Nguyen.blogplatform.domain.notification.application.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentNotificationDispatcher {
    private final NotificationService notificationService;

    @Async
    public void dispatch(String authorId, String commenterId, String commenterName,
                         String postTitle, String postId, CommentResponse response) {
        try {
            if (!authorId.equals(commenterId)) {
                notificationService.notifyPostAuthorAboutComment(authorId, commenterName, postTitle, postId);
            }
        } catch (Exception e) {
            log.warn("Failed to notify post author '{}': {}", authorId, e.getMessage());
        }
        try {
            notificationService.sendCommentNotification(postId, response);
            notificationService.sendGlobalNotification("New comment from: " + commenterName + " on post: " + postTitle);
        } catch (Exception e) {
            log.warn("Failed to send real-time notification: {}", e.getMessage());
        }
    }
}
