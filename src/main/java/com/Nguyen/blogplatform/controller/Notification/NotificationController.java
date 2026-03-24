package com.Nguyen.blogplatform.controller.Notification;

import com.Nguyen.blogplatform.payload.request.notification.SendNotificationRequest;
import com.Nguyen.blogplatform.payload.request.notification.UpdatePreferencesRequest;
import com.Nguyen.blogplatform.payload.response.MessageResponse;
import com.Nguyen.blogplatform.payload.response.notification.NotificationHistoryResponse;
import com.Nguyen.blogplatform.payload.response.notification.NotificationResponse;
import com.Nguyen.blogplatform.payload.response.notification.UserPreferencesResponse;
import com.Nguyen.blogplatform.service.auth.UserDetailsImpl;
import com.Nguyen.blogplatform.service.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management APIs")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @Operation(summary = "Send notification to users")
    public ResponseEntity<MessageResponse> sendNotification(@Valid @RequestBody SendNotificationRequest request) {
        if (request.isBulk() && request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            notificationService.sendBulkNotifications(
                request.getUserIds(),
                request.getTemplateCode(),
                request.getTemplateData(),
                request.getChannels()
            );
        } else if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            for (String userId : request.getUserIds()) {
                notificationService.sendNotificationAsync(
                    userId,
                    request.getTemplateCode(),
                    request.getTemplateData(),
                    request.getChannels()
                );
            }
        }

        return ResponseEntity.ok(new MessageResponse("Notifications queued successfully"));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user notifications")
    public ResponseEntity<Page<NotificationHistoryResponse>> getUserNotifications(
        @PathVariable String userId,
        Pageable pageable,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (!userDetails.getId().equals(userId) && !isAdmin(userDetails)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(notificationService.getUserNotifications(userId, pageable));
    }

    @GetMapping("/user/{userId}/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<Long> getUnreadCount(
        @PathVariable String userId,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (!userDetails.getId().equals(userId) && !isAdmin(userDetails)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<MessageResponse> markAsRead(
        @PathVariable String id,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {

        notificationService.markAsRead(id, userDetails.getId());
        return ResponseEntity.ok(new MessageResponse("Notification marked as read"));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<MessageResponse> markAllAsRead(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        notificationService.markAllAsRead(userDetails.getId());
        return ResponseEntity.ok(new MessageResponse("All notifications marked as read"));
    }

    @PostMapping("/preferences")
    @Operation(summary = "Update user notification preferences")
    public ResponseEntity<MessageResponse> updatePreferences(
        @Valid @RequestBody UpdatePreferencesRequest request,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {

        notificationService.updatePreferences(userDetails.getId(), request);
        return ResponseEntity.ok(new MessageResponse("Preferences updated successfully"));
    }

    @GetMapping("/preferences")
    @Operation(summary = "Get user notification preferences")
    public ResponseEntity<UserPreferencesResponse> getPreferences(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        var prefs = notificationService.getUserPreferences(userDetails.getId());
        return ResponseEntity.ok(UserPreferencesResponse.builder()
            .userId(userDetails.getId())
            .preferences(prefs)
            .build());
    }

    // Legacy endpoints for backward compatibility
    @GetMapping
    @Operation(summary = "Get notifications of current user (Legacy)")
    public ResponseEntity<List<com.Nguyen.blogplatform.model.Notifications>> getNotificationsOfUser(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(notificationService.getNotificationsOfUser(userDetails.getId()));
    }

    private boolean isAdmin(UserDetailsImpl userDetails) {
        return userDetails.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}



