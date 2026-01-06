package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.Enum.NewsletterFrequency;
import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.model.Newsletter;
import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.payload.request.NewsletterSubscriptionRequest;
import com.Nguyen.blogplatform.payload.response.MessageResponse;
import com.Nguyen.blogplatform.payload.response.NewsletterResponse;
import com.Nguyen.blogplatform.repository.NewsletterRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsletterService {
    
    private final NewsletterRepository newsletterRepository;
    private final EmailServices emailServices;
    
    @Transactional
    public MessageResponse subscribe(NewsletterSubscriptionRequest request) {
        // Check if email already exists
        if (newsletterRepository.existsByEmail(request.getEmail())) {
            Newsletter existing = newsletterRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new NotFoundException("Newsletter subscription not found"));
            
            if (existing.getIsActive() && existing.getIsConfirmed()) {
                return new MessageResponse("You are already subscribed to our newsletter!");
            }
            
            if (existing.getIsActive()) {
                // Resend confirmation email
                try {
                    emailServices.sendNewsletterConfirmationEmail(
                        existing.getEmail(), 
                        existing.getName(), 
                        existing.getConfirmationToken()
                    );
                    return new MessageResponse("Confirmation email has been resent. Please check your email to confirm your subscription.");
                } catch (MessagingException e) {
                    log.error("Failed to send confirmation email", e);
                    return new MessageResponse("Failed to send confirmation email. Please try again later.");
                }
            }
            
            // Reactivate if previously unsubscribed
            existing.setIsActive(true);
            existing.setUnsubscribedAt(null);
            existing.setName(request.getName());
            newsletterRepository.save(existing);
            
            try {
                emailServices.sendNewsletterConfirmationEmail(
                    existing.getEmail(), 
                    existing.getName(), 
                    existing.getConfirmationToken()
                );
                return new MessageResponse("Welcome back! Please check your email to confirm your subscription.");
            } catch (MessagingException e) {
                log.error("Failed to send confirmation email", e);
                return new MessageResponse("Subscription reactivated but failed to send confirmation email.");
            }
        }
        
        // Create new subscription
        String confirmationToken = UUID.randomUUID().toString();
        String subscriptionToken = UUID.randomUUID().toString();

        Newsletter newsletter = Newsletter.builder()
                .email(request.getEmail())
                .name(request.getName())
                .frequency(
                        request.getFrequency() != null
                                ? request.getFrequency()
                                : NewsletterFrequency.DAILY
                )
                .isActive(true)
                .isConfirmed(false)
                .confirmationToken(confirmationToken)
                .subscriptionToken(subscriptionToken)
                .build();
        
        newsletterRepository.save(newsletter);
        
        try {
            emailServices.sendNewsletterConfirmationEmail(
                request.getEmail(), 
                request.getName(), 
                confirmationToken
            );
            return new MessageResponse("Thank you for subscribing! Please check your email to confirm your subscription.");
        } catch (MessagingException e) {
            log.error("Failed to send confirmation email", e);
            return new MessageResponse("Subscription created but failed to send confirmation email. Please contact support.");
        }
    }
    
    @Transactional
    public MessageResponse confirmSubscription(String confirmationToken) {
        Newsletter newsletter = newsletterRepository.findByConfirmationToken(confirmationToken)
                .orElseThrow(() -> new NotFoundException("Invalid confirmation token"));
        
        if (newsletter.getIsConfirmed()) {
            return new MessageResponse("Your subscription is already confirmed!");
        }
        
        newsletter.setIsConfirmed(true);
        newsletter.setConfirmedAt(LocalDateTime.now());
        newsletter.setConfirmationToken(null); // Clear the token after confirmation
        
        newsletterRepository.save(newsletter);
        
        return new MessageResponse("Your newsletter subscription has been confirmed successfully!");
    }
    
    @Transactional
    public MessageResponse unsubscribe(String subscriptionToken) {
        Newsletter newsletter = newsletterRepository.findBySubscriptionToken(subscriptionToken)
                .orElseThrow(() -> new NotFoundException("Invalid unsubscribe token"));
        
        if (!newsletter.getIsActive()) {
            return new MessageResponse("You are already unsubscribed from our newsletter.");
        }
        
        newsletter.setIsActive(false);
        newsletter.setUnsubscribedAt(LocalDateTime.now());
        
        newsletterRepository.save(newsletter);
        
        return new MessageResponse("You have been successfully unsubscribed from our newsletter.");
    }
    
    public Page<NewsletterResponse> getAllSubscribers(Pageable pageable) {
        return newsletterRepository.findAll(pageable)
                .map(this::toNewsletterResponse);
    }
    
    public Page<NewsletterResponse> getActiveSubscribers(Pageable pageable) {
        return newsletterRepository.findAllActiveAndConfirmedSubscribers(pageable)
                .map(this::toNewsletterResponse);
    }
    
    public Long getActiveSubscriberCount() {
        return newsletterRepository.countActiveAndConfirmedSubscribers();
    }
    
//    @Transactional
//    public void sendNewsletterForNewPost(Post post) {
//        List<Newsletter> activeSubscribers = newsletterRepository.findAllActiveAndConfirmedSubscribers();
//
//        if (!activeSubscribers.isEmpty()) {
//            try {
//                emailServices.sendNewsletterEmail(activeSubscribers, post);
//                log.info("Newsletter sent to {} subscribers for post: {}", activeSubscribers.size(), post.getTitle());
//            } catch (MessagingException e) {
//                log.error("Failed to send newsletter for post: {}", post.getTitle(), e);
//            }
//        }
//    }
    
    private NewsletterResponse toNewsletterResponse(Newsletter newsletter) {
        return NewsletterResponse.builder()
                .id(newsletter.getId())
                .email(newsletter.getEmail())
                .name(newsletter.getName())
                .isActive(newsletter.getIsActive())
                .isConfirmed(newsletter.getIsConfirmed())
                .subscribedAt(newsletter.getSubscribedAt())
                .confirmedAt(newsletter.getConfirmedAt())
                .unsubscribedAt(newsletter.getUnsubscribedAt())
                .build();
    }


    @Transactional
    public MessageResponse updateFrequency(
            String subscriptionToken,
            NewsletterFrequency frequency
    ) {
        Newsletter newsletter = newsletterRepository
                .findBySubscriptionToken(subscriptionToken)
                .orElseThrow(() -> new NotFoundException("Invalid token"));

        newsletter.setFrequency(frequency);
        newsletterRepository.save(newsletter);

        return new MessageResponse(
                "Newsletter frequency updated to " + frequency
        );
    }
}
