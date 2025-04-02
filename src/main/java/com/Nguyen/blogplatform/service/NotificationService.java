package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.payload.response.CommentResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service

public class NotificationService {
    public final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendPostNotification(String postId, String message) {
        messagingTemplate.convertAndSend("/topic/post/" + postId, message);
    }
    public void sendCommentNotification(String postId, CommentResponse comment) {
        messagingTemplate.convertAndSend("/topic/comments/" + postId, comment);
    }

    public void sendGlobalNotification(String message) {
        messagingTemplate.convertAndSend("/topic/global", message);
    }

}
