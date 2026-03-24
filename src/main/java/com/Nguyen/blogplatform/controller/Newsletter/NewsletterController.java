package com.Nguyen.blogplatform.controller.Newsletter;

import com.Nguyen.blogplatform.Enum.ENewsletterStatus;
import com.Nguyen.blogplatform.payload.request.newsletter.*;
import com.Nguyen.blogplatform.payload.response.MessageResponse;
import com.Nguyen.blogplatform.payload.response.newsletter.*;
import com.Nguyen.blogplatform.service.auth.UserDetailsImpl;
import com.Nguyen.blogplatform.service.newsletter.NewsletterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/newsletter")
@RequiredArgsConstructor
@Tag(name = "Newsletter", description = "Newsletter management APIs")
public class NewsletterController {

    private final NewsletterService newsletterService;

    @PostMapping("/subscribe")
    @Operation(summary = "Subscribe to newsletter with double opt-in")
    public ResponseEntity<SubscriptionResponse> subscribe(
        @Valid @RequestBody SubscribeRequest request,
        HttpServletRequest httpRequest) {

        String ipAddress = getClientIp(httpRequest);
        return ResponseEntity.ok(newsletterService.subscribe(request, ipAddress));
    }

    @GetMapping("/confirm/{token}")
    @Operation(summary = "Confirm subscription with token")
    public ResponseEntity<MessageResponse> confirmSubscription(
        @PathVariable String token,
        HttpServletRequest httpRequest) {

        String ipAddress = getClientIp(httpRequest);
        return ResponseEntity.ok(newsletterService.confirmSubscription(token, ipAddress));
    }

    @PostMapping("/unsubscribe")
    @Operation(summary = "Unsubscribe via API")
    public ResponseEntity<MessageResponse> unsubscribe(@Valid @RequestBody UnsubscribeRequest request) {
        return ResponseEntity.ok(newsletterService.unsubscribeByEmail(request));
    }

    @GetMapping("/unsubscribe/{token}")
    @Operation(summary = "One-click unsubscribe with token")
    public ResponseEntity<MessageResponse> unsubscribeByToken(@PathVariable String token) {
        return ResponseEntity.ok(newsletterService.unsubscribe(token));
    }

    @GetMapping("/subscribers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all subscribers (Admin only)")
    public ResponseEntity<Page<SubscriberResponse>> getSubscribers(
        Pageable pageable,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) ENewsletterStatus status) {

        return ResponseEntity.ok(newsletterService.getSubscribers(pageable, search, status));
    }

    @GetMapping("/subscribers/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get active subscriber count (Admin only)")
    public ResponseEntity<Long> getActiveSubscriberCount() {
        return ResponseEntity.ok(newsletterService.getActiveSubscriberCount());
    }

    @PostMapping("/campaigns")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create and schedule a campaign (Admin only)")
    public ResponseEntity<CampaignResponse> createCampaign(
        @Valid @RequestBody CreateCampaignRequest request,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {

        return ResponseEntity.ok(newsletterService.createCampaign(request, userDetails.getId()));
    }

    @PostMapping("/campaigns/{id}/send")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Send campaign immediately (Admin only)")
    public ResponseEntity<MessageResponse> sendCampaign(@PathVariable String id) {
        newsletterService.sendCampaign(id);
        return ResponseEntity.ok(new MessageResponse("Campaign is being sent"));
    }

    @GetMapping("/campaigns")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all campaigns (Admin only)")
    public ResponseEntity<Page<CampaignResponse>> getCampaigns(Pageable pageable) {
        return ResponseEntity.ok(newsletterService.getCampaigns(pageable));
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Bulk import subscribers (Admin only)")
    public ResponseEntity<ImportResultResponse> importSubscribers(
        @Valid @RequestBody BulkImportRequest request) {

        return ResponseEntity.ok(newsletterService.importSubscribers(request));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
