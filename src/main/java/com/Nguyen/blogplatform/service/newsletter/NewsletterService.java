package com.Nguyen.blogplatform.service.newsletter;

import com.Nguyen.blogplatform.Enum.ECampaignStatus;
import com.Nguyen.blogplatform.Enum.ENewsletterStatus;
import com.Nguyen.blogplatform.model.*;
import com.Nguyen.blogplatform.payload.request.newsletter.*;
import com.Nguyen.blogplatform.payload.response.MessageResponse;
import com.Nguyen.blogplatform.payload.response.newsletter.*;
import com.Nguyen.blogplatform.repository.*;
import com.Nguyen.blogplatform.service.notification.EmailServices;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsletterService {

    private final NewsletterSubscriberRepository subscriberRepository;
    private final NewsletterCampaignRepository campaignRepository;
    private final EmailLogRepository emailLogRepository;
    private final EmailServices emailServices;
    private final ObjectMapper objectMapper;
    private final TemplateEngine templateEngine;

    @Value("${newsletter.confirmation.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${newsletter.from-email:noreply@blogplatform.com}")
    private String fromEmail;

    @Value("${newsletter.batch.size:100}")
    private int batchSize;

    @Transactional
    public SubscriptionResponse subscribe(SubscribeRequest request, String ipAddress) {
        Optional<NewsletterSubscriber> existing = subscriberRepository.findByEmail(request.getEmail());

        if (existing.isPresent()) {
            NewsletterSubscriber subscriber = existing.get();

            if (subscriber.getStatus() == ENewsletterStatus.ACTIVE) {
                return SubscriptionResponse.builder()
                    .success(false)
                    .message("You are already subscribed to our newsletter!")
                    .build();
            }

            if (subscriber.getStatus() == ENewsletterStatus.PENDING) {
                resendConfirmationEmail(subscriber);
                return SubscriptionResponse.builder()
                    .success(true)
                    .message("Confirmation email has been resent. Please check your inbox.")
                    .requiresConfirmation(true)
                    .build();
            }

            subscriber.setStatus(ENewsletterStatus.PENDING);
            subscriber.setConfirmationToken(UUID.randomUUID().toString());
            subscriber.setConfirmationTokenExpiresAt(LocalDateTime.now().plusHours(24));
            subscriber.setFirstName(request.getFirstName());
            subscriber.setLastName(request.getLastName());
            subscriber.setGdprConsent(request.isGdprConsent());
            subscriber.setGdprConsentAt(request.isGdprConsent() ? LocalDateTime.now() : null);
            subscriberRepository.save(subscriber);

            sendConfirmationEmail(subscriber);

            return SubscriptionResponse.builder()
                .success(true)
                .message("Welcome back! Please confirm your subscription via email.")
                .requiresConfirmation(true)
                .build();
        }

        NewsletterSubscriber newSubscriber = NewsletterSubscriber.builder()
            .email(request.getEmail().toLowerCase())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .sourceUrl(request.getSourceUrl())
            .signupIp(ipAddress)
            .gdprConsent(request.isGdprConsent())
            .gdprConsentAt(request.isGdprConsent() ? LocalDateTime.now() : null)
            .status(ENewsletterStatus.PENDING)
            .confirmationToken(UUID.randomUUID().toString())
            .confirmationTokenExpiresAt(LocalDateTime.now().plusHours(24))
            .build();

        subscriberRepository.save(newSubscriber);
        sendConfirmationEmail(newSubscriber);

        return SubscriptionResponse.builder()
            .success(true)
            .message("Please check your email to confirm your subscription.")
            .requiresConfirmation(true)
            .build();
    }

    @Transactional
    public MessageResponse confirmSubscription(String token, String ipAddress) {
        NewsletterSubscriber subscriber = subscriberRepository.findByConfirmationToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid or expired confirmation token"));

        if (subscriber.getStatus() == ENewsletterStatus.ACTIVE) {
            return new MessageResponse("Your subscription is already confirmed!");
        }

        if (subscriber.getConfirmationTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Confirmation token has expired. Please subscribe again.");
        }

        subscriberRepository.confirmSubscription(subscriber.getId(), LocalDateTime.now());
        subscriber.setConfirmedIp(ipAddress);

        sendWelcomeEmail(subscriber);

        return new MessageResponse("Your subscription has been confirmed successfully!");
    }

    @Transactional
    public MessageResponse unsubscribe(String token) {
        NewsletterSubscriber subscriber = subscriberRepository.findByUnsubscribeToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid unsubscribe token"));

        if (subscriber.getStatus() == ENewsletterStatus.UNSUBSCRIBED) {
            return new MessageResponse("You are already unsubscribed.");
        }

        subscriberRepository.unsubscribe(subscriber.getId(), LocalDateTime.now());

        return new MessageResponse("You have been successfully unsubscribed.");
    }

    @Transactional
    public MessageResponse unsubscribeByEmail(UnsubscribeRequest request) {
        NewsletterSubscriber subscriber = subscriberRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Email not found in our subscription list"));

        subscriberRepository.unsubscribe(subscriber.getId(), LocalDateTime.now());

        return new MessageResponse("You have been successfully unsubscribed.");
    }

    @Transactional(readOnly = true)
    public Page<SubscriberResponse> getSubscribers(Pageable pageable, String search, ENewsletterStatus status) {
        Page<NewsletterSubscriber> subscribers;

        if (search != null && !search.isEmpty()) {
            subscribers = subscriberRepository.findByEmailContainingIgnoreCase(search, pageable);
        } else if (status != null) {
            subscribers = subscriberRepository.findByStatus(status, pageable);
        } else {
            subscribers = subscriberRepository.findAll(pageable);
        }

        return subscribers.map(this::mapToSubscriberResponse);
    }

    @Transactional(readOnly = true)
    public long getActiveSubscriberCount() {
        return subscriberRepository.countActiveSubscribers();
    }

    @Transactional
    public CampaignResponse createCampaign(CreateCampaignRequest request, String userId) {
        NewsletterCampaign campaign = NewsletterCampaign.builder()
            .name(request.getName())
            .subject(request.getSubject())
            .htmlContent(request.getHtmlContent())
            .textContent(request.getTextContent())
            .fromName(request.getFromName())
            .fromEmail(request.getFromEmail() != null ? request.getFromEmail() : fromEmail)
            .replyTo(request.getReplyTo())
            .targetSegment(request.getTargetSegment())
            .targetTags(request.getTargetTags())
            .scheduledAt(request.getScheduledAt())
            .batchSize(request.getBatchSize() != null ? request.getBatchSize() : batchSize)
            .sendIntervalSeconds(request.getSendIntervalSeconds() != null ? request.getSendIntervalSeconds() : 1)
            .status(request.getScheduledAt() != null ? ECampaignStatus.SCHEDULED : ECampaignStatus.DRAFT)
            .utmSource(request.getUtmSource())
            .utmMedium(request.getUtmMedium())
            .utmCampaign(request.getUtmCampaign())
            .build();

        NewsletterCampaign saved = campaignRepository.save(campaign);

        return mapToCampaignResponse(saved);
    }

    @Async("newsletterTaskExecutor")
    @Transactional
    public void sendCampaign(String campaignId) {
        NewsletterCampaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));

        if (campaign.getStatus() != ECampaignStatus.DRAFT && campaign.getStatus() != ECampaignStatus.SCHEDULED) {
            throw new RuntimeException("Campaign cannot be sent. Current status: " + campaign.getStatus());
        }

        campaignRepository.updateStatus(campaignId, ECampaignStatus.SENDING);

        List<NewsletterSubscriber> subscribers = getTargetSubscribers(campaign);
        campaign.setRecipientCount((long) subscribers.size());
        campaignRepository.save(campaign);

        for (NewsletterSubscriber subscriber : subscribers) {
            try {
                sendEmailToSubscriber(campaign, subscriber);
                campaignRepository.incrementSentCount(campaignId);

                Thread.sleep(campaign.getSendIntervalSeconds() * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Campaign sending interrupted", e);
                break;
            } catch (Exception e) {
                log.error("Failed to send email to: {}", subscriber.getEmail(), e);
            }
        }

        campaignRepository.updateStatus(campaignId, ECampaignStatus.SENT);
        campaign.setSentAt(LocalDateTime.now());
        campaignRepository.save(campaign);

        log.info("Campaign {} sent to {} subscribers", campaignId, campaign.getSentCount());
    }

    @Transactional
    public ImportResultResponse importSubscribers(BulkImportRequest request) {
        int imported = 0;
        int skipped = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        for (SubscriberImportRow row : request.getSubscribers()) {
            try {
                if (subscriberRepository.existsByEmail(row.getEmail())) {
                    skipped++;
                    continue;
                }

                NewsletterSubscriber subscriber = NewsletterSubscriber.builder()
                    .email(row.getEmail().toLowerCase())
                    .firstName(row.getFirstName())
                    .lastName(row.getLastName())
                    .tags(row.getTags())
                    .status(request.isRequireConfirmation() ? ENewsletterStatus.PENDING : ENewsletterStatus.ACTIVE)
                    .gdprConsent(row.isGdprConsent())
                    .gdprConsentAt(row.isGdprConsent() ? LocalDateTime.now() : null)
                    .sourceUrl("bulk-import")
                    .build();

                if (!request.isRequireConfirmation()) {
                    subscriber.setConfirmedAt(LocalDateTime.now());
                }

                subscriberRepository.save(subscriber);
                imported++;

                if (request.isSendWelcomeEmail() && !request.isRequireConfirmation()) {
                    sendWelcomeEmail(subscriber);
                }

            } catch (Exception e) {
                failed++;
                errors.add("Failed to import " + row.getEmail() + ": " + e.getMessage());
                log.error("Failed to import subscriber: {}", row.getEmail(), e);
            }
        }

        return ImportResultResponse.builder()
            .imported(imported)
            .skipped(skipped)
            .failed(failed)
            .errors(errors)
            .build();
    }

    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void processScheduledCampaigns() {
        List<NewsletterCampaign> scheduledCampaigns = campaignRepository
            .findByStatusAndScheduledAtBefore(ECampaignStatus.SCHEDULED, LocalDateTime.now());

        for (NewsletterCampaign campaign : scheduledCampaigns) {
            try {
                sendCampaign(campaign.getId());
            } catch (Exception e) {
                log.error("Failed to process scheduled campaign: {}", campaign.getId(), e);
            }
        }
    }

    @Scheduled(cron = "0 0 */6 * * *")
    @Transactional
    public void cleanupExpiredPendingSubscriptions() {
        List<NewsletterSubscriber> expired = subscriberRepository
            .findExpiredPendingSubscriptions(LocalDateTime.now());

        for (NewsletterSubscriber subscriber : expired) {
            subscriberRepository.delete(subscriber);
            log.info("Cleaned up expired pending subscription: {}", subscriber.getEmail());
        }
    }

    private void sendConfirmationEmail(NewsletterSubscriber subscriber) {
        String confirmUrl = baseUrl + "/api/v1/newsletter/confirm/" + subscriber.getConfirmationToken();

        try {
            emailServices.sendHtmlEmail(
                subscriber.getEmail(),
                "Confirm Your Newsletter Subscription",
                buildConfirmationEmail(subscriber.getFirstName(), confirmUrl)
            );
        } catch (Exception e) {
            log.error("Failed to send confirmation email to: {}", subscriber.getEmail(), e);
        }
    }

    private void resendConfirmationEmail(NewsletterSubscriber subscriber) {
        subscriber.setConfirmationToken(UUID.randomUUID().toString());
        subscriber.setConfirmationTokenExpiresAt(LocalDateTime.now().plusHours(24));
        subscriberRepository.save(subscriber);
        sendConfirmationEmail(subscriber);
    }

    private void sendWelcomeEmail(NewsletterSubscriber subscriber) {
        try {
            String unsubscribeUrl = baseUrl + "/api/v1/newsletter/unsubscribe/" + subscriber.getUnsubscribeToken();
            emailServices.sendHtmlEmail(
                subscriber.getEmail(),
                "Welcome to Our Newsletter!",
                buildWelcomeEmail(subscriber.getFirstName(), unsubscribeUrl, baseUrl)
            );
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", subscriber.getEmail(), e);
        }
    }

    private void sendEmailToSubscriber(NewsletterCampaign campaign, NewsletterSubscriber subscriber) {
        String unsubscribeUrl = baseUrl + "/api/v1/newsletter/unsubscribe/" + subscriber.getUnsubscribeToken();

        String personalizedHtml = campaign.getHtmlContent()
            .replace("{{firstName}}", subscriber.getFirstName() != null ? subscriber.getFirstName() : "Subscriber")
            .replace("{{email}}", subscriber.getEmail())
            .replace("{{unsubscribeUrl}}", unsubscribeUrl);

        try {
            emailServices.sendHtmlEmail(
                subscriber.getEmail(),
                campaign.getSubject(),
                personalizedHtml
            );

            EmailLog log = EmailLog.builder()
                .campaignId(campaign.getId())
                .subscriberId(subscriber.getId())
                .recipientEmail(subscriber.getEmail())
                .subject(campaign.getSubject())
                .status(EmailLog.EmailStatus.SENT)
                .sentAt(LocalDateTime.now())
                .build();

            emailLogRepository.save(log);

            subscriber.setLastSentAt(LocalDateTime.now());
            subscriberRepository.save(subscriber);

        } catch (Exception e) {
            log.error("Failed to send campaign email to: {}", subscriber.getEmail(), e);

            EmailLog emailLog = EmailLog.builder()
                .campaignId(campaign.getId())
                .subscriberId(subscriber.getId())
                .recipientEmail(subscriber.getEmail())
                .subject(campaign.getSubject())
                .status(EmailLog.EmailStatus.FAILED)
                .errorMessage(e.getMessage())
                .build();

            emailLogRepository.save(emailLog);
        }
    }

    private List<NewsletterSubscriber> getTargetSubscribers(NewsletterCampaign campaign) {
        if (campaign.getTargetTags() != null && !campaign.getTargetTags().isEmpty()) {
            return subscriberRepository.findActiveByTag(campaign.getTargetTags());
        }
        return subscriberRepository.findByStatus(ENewsletterStatus.ACTIVE, Pageable.unpaged()).getContent();
    }

    @Transactional(readOnly = true)
    public Page<CampaignResponse> getCampaigns(Pageable pageable) {
        return campaignRepository.findAll(pageable)
            .map(this::mapToCampaignResponse);
    }

    private String buildConfirmationEmail(String firstName, String confirmUrl) {
        Context context = new Context();
        context.setVariable("name", firstName != null ? firstName : "there");
        context.setVariable("confirmationUrl", confirmUrl);
        return templateEngine.process("newsletter-subscription-confirmation", context);
    }

    private String buildWelcomeEmail(String firstName, String unsubscribeUrl, String blogUrl) {
        Context context = new Context();
        context.setVariable("firstName", firstName != null ? firstName : "Subscriber");
        context.setVariable("unsubscribeUrl", unsubscribeUrl);
        context.setVariable("blogUrl", blogUrl != null ? blogUrl : baseUrl);
        return templateEngine.process("newsletter-welcome", context);
    }

    private SubscriberResponse mapToSubscriberResponse(NewsletterSubscriber subscriber) {
        return SubscriberResponse.builder()
            .id(subscriber.getId())
            .email(subscriber.getEmail())
            .firstName(subscriber.getFirstName())
            .lastName(subscriber.getLastName())
            .status(subscriber.getStatus())
            .createdAt(subscriber.getCreatedAt())
            .confirmedAt(subscriber.getConfirmedAt())
            .unsubscribedAt(subscriber.getUnsubscribedAt())
            .lastSentAt(subscriber.getLastSentAt())
            .tags(subscriber.getTags())
            .build();
    }

    private CampaignResponse mapToCampaignResponse(NewsletterCampaign campaign) {
        return CampaignResponse.builder()
            .id(campaign.getId())
            .name(campaign.getName())
            .subject(campaign.getSubject())
            .status(campaign.getStatus())
            .scheduledAt(campaign.getScheduledAt())
            .sentAt(campaign.getSentAt())
            .recipientCount(campaign.getRecipientCount())
            .sentCount(campaign.getSentCount())
            .openedCount(campaign.getOpenedCount())
            .clickedCount(campaign.getClickedCount())
            .bouncedCount(campaign.getBouncedCount())
            .unsubscribedCount(campaign.getUnsubscribedCount())
            .createdAt(campaign.getCreatedAt())
            .build();
    }
}
