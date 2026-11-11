package com.Nguyen.blogplatform.shared.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Authorization model: every user can write and manage their OWN content;
 * ROLE_ADMIN manages ALL content on the platform (posts, series, comments, users).
 */
public final class SecurityUtils {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    private SecurityUtils() {
    }

    public static boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && auth.getAuthorities().stream()
                .anyMatch(a -> ROLE_ADMIN.equals(a.getAuthority()));
    }

    /** Owner of the resource, or an admin. */
    public static boolean isOwnerOrAdmin(String ownerId, String currentUserId) {
        return (ownerId != null && ownerId.equals(currentUserId)) || isCurrentUserAdmin();
    }
}
