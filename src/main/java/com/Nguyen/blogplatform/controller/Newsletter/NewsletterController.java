package com.Nguyen.blogplatform.controller.Newsletter;

import com.Nguyen.blogplatform.payload.request.NewsletterSubscriptionRequest;
import com.Nguyen.blogplatform.payload.response.MessageResponse;
import com.Nguyen.blogplatform.payload.response.NewsletterResponse;
import com.Nguyen.blogplatform.service.NewsletterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/newsletter")
@RequiredArgsConstructor
@Tag(name = "Newsletter", description = "Newsletter subscription management")
public class NewsletterController {
    
    private final NewsletterService newsletterService;
    
    @PostMapping("/subscribe")
    @Operation(summary = "Subscribe to newsletter", description = "Subscribe to the newsletter with email and optional name")
    public ResponseEntity<MessageResponse> subscribe(@Valid @RequestBody NewsletterSubscriptionRequest request) {
        MessageResponse response = newsletterService.subscribe(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/confirm")
    @Operation(summary = "Confirm newsletter subscription", description = "Confirm newsletter subscription using confirmation token")
    public ResponseEntity<MessageResponse> confirmSubscription(
            @Parameter(description = "Confirmation token") @RequestParam String token) {
        MessageResponse response = newsletterService.confirmSubscription(token);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/unsubscribe")
    @Operation(summary = "Unsubscribe from newsletter", description = "Unsubscribe from newsletter using subscription token")
    public ResponseEntity<MessageResponse> unsubscribe(
            @Parameter(description = "Subscription token") @RequestParam String token) {
        MessageResponse response = newsletterService.unsubscribe(token);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/subscribers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all subscribers", description = "Get paginated list of all newsletter subscribers (Admin only)")
    public ResponseEntity<Page<NewsletterResponse>> getAllSubscribers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsletterResponse> subscribers = newsletterService.getAllSubscribers(pageable);
        return ResponseEntity.ok(subscribers);
    }
    
    @GetMapping("/subscribers/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get active subscribers", description = "Get paginated list of active and confirmed newsletter subscribers (Admin only)")
    public ResponseEntity<Page<NewsletterResponse>> getActiveSubscribers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsletterResponse> subscribers = newsletterService.getActiveSubscribers(pageable);
        return ResponseEntity.ok(subscribers);
    }
    
    @GetMapping("/subscribers/count")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get active subscriber count", description = "Get total count of active and confirmed newsletter subscribers (Admin only)")
    public ResponseEntity<Long> getActiveSubscriberCount() {
        Long count = newsletterService.getActiveSubscriberCount();
        return ResponseEntity.ok(count);
    }
}
