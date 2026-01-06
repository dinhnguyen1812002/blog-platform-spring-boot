package com.Nguyen.blogplatform.controller.Newsletter;


import com.Nguyen.blogplatform.Enum.NewsletterFrequency;
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
    @Operation(
            summary = "Subscribe to newsletter",
            description = "Subscribe with email, name and frequency (DAILY / WEEKLY)"
    )
    public ResponseEntity<MessageResponse> subscribe(
            @Valid @RequestBody NewsletterSubscriptionRequest request
    ) {
        return ResponseEntity.ok(newsletterService.subscribe(request));
    }

    @GetMapping("/confirm")
    @Operation(
            summary = "Confirm newsletter subscription",
            description = "Confirm subscription using confirmation token"
    )
    public ResponseEntity<MessageResponse> confirmSubscription(
            @RequestParam("token") String token
    ) {
        return ResponseEntity.ok(
                newsletterService.confirmSubscription(token)
        );
    }

    @GetMapping("/unsubscribe")
    @Operation(
            summary = "Unsubscribe from newsletter",
            description = "Unsubscribe using subscription token"
    )
    public ResponseEntity<MessageResponse> unsubscribe(
            @RequestParam("token") String token
    ) {
        return ResponseEntity.ok(
                newsletterService.unsubscribe(token)
        );
    }



    @PatchMapping("/frequency")
    @Operation(
            summary = "Update newsletter frequency",
            description = "Update subscription frequency (DAILY / WEEKLY)"
    )
    public ResponseEntity<MessageResponse> updateFrequency(
            @RequestParam("token") String subscriptionToken,
            @RequestParam("frequency") NewsletterFrequency frequency
    ) {
        return ResponseEntity.ok(
                newsletterService.updateFrequency(subscriptionToken, frequency)
        );
    }


    @GetMapping("/subscribers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all subscribers",
            description = "Paginated list of all newsletter subscribers (Admin only)"
    )
    public ResponseEntity<Page<NewsletterResponse>> getAllSubscribers(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                newsletterService.getAllSubscribers(pageable)
        );
    }

    @GetMapping("/subscribers/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get active subscribers",
            description = "Active & confirmed subscribers (Admin only)"
    )
    public ResponseEntity<Page<NewsletterResponse>> getActiveSubscribers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                newsletterService.getActiveSubscribers(pageable)
        );
    }

    @GetMapping("/subscribers/count")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get active subscriber count",
            description = "Count active & confirmed subscribers (Admin only)"
    )
    public ResponseEntity<Long> getActiveSubscriberCount() {
        return ResponseEntity.ok(
                newsletterService.getActiveSubscriberCount()
        );
    }
}
