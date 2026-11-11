package com.Nguyen.blogplatform.infrastructure.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * Blocks STOMP subscriptions to admin-only topics (e.g. live application logs)
 * for non-admin sessions. Both /ws and /ws-logs share the same broker, so the
 * HTTP-level rule on /ws-logs alone is not enough.
 */
@Component
public class AdminTopicChannelInterceptor implements ChannelInterceptor {

    private static final String ADMIN_TOPIC_PREFIX = "/topic/logs";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() != StompCommand.SUBSCRIBE) {
            return message;
        }

        String destination = accessor.getDestination();
        if (destination != null && destination.startsWith(ADMIN_TOPIC_PREFIX) && !isAdmin(accessor.getUser())) {
            throw new MessageDeliveryException("Access denied to " + destination);
        }
        return message;
    }

    private boolean isAdmin(Principal principal) {
        return principal instanceof Authentication auth
                && auth.isAuthenticated()
                && auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
