package com.Nguyen.blogplatform.service.notification;

import com.Nguyen.blogplatform.payload.EmailEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import glide.api.GlideClient;
import glide.api.models.commands.SetOptions;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Service to handle Email Queueing (via Valkey Stream) and Sending.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServices {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final GlideClient glideClient;
    private final ObjectMapper objectMapper;

    @Value("${base-url}")
    private String baseUrl;

    private static final String EMAIL_STREAM_KEY = "stream:emails";

    /**
     * Add an email event to the Valkey Stream (XADD).
     */
    public void queueEmail(EmailEvent event) {
        try {
            String jsonEvent = objectMapper.writeValueAsString(event);
            // XADD stream:emails * event <json>
            glideClient.xadd(EMAIL_STREAM_KEY, Map.of("event", jsonEvent)).get();
            log.info("Email queued: type={}, recipient={}", event.type(), event.recipient());
        } catch (JsonProcessingException | InterruptedException | ExecutionException e) {
            log.error("Failed to queue email: {}", event.recipient(), e);
            throw new RuntimeException("Email queueing failed", e);
        }
    }

    /**
     * The actual execution of sending an email, called by the consumer.
     */
    public void sendEmail(EmailEvent event) throws MessagingException {
        Context context = new Context();
        if (event.data() != null) {
            event.data().forEach(context::setVariable);
        }

        String htmlContent;
        if (event.template() != null && !event.template().isBlank()) {
            htmlContent = templateEngine.process(event.template(), context);
        } else {
            htmlContent = (String) event.data().get("content");
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("noreply@blog.com");
        helper.setTo(event.recipient());
        helper.setSubject(event.subject());
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("Email sent successfully to: {}", event.recipient());
    }

    /* Helper methods to queue specific emails */

    public void queuePasswordResetEmail(String to, String resetUrl) {
        EmailEvent event = new EmailEvent(
            EmailEvent.TYPE_PASSWORD_RESET,
            to,
            "Password Reset Request",
            null, // No template, uses raw text or custom logic
            Map.of("content", "<p>Hello,</p><p>You requested to reset your password.</p><p><a href=\"" + resetUrl + "\">Reset Password</a></p>")
        );
        queueEmail(event);
    }

    public void queueNewsletterConfirmationEmail(String to, String name, String confirmationToken) {
        String confirmationUrl = baseUrl + "/api/v1/newsletter/confirm?token=" + confirmationToken;
        EmailEvent event = new EmailEvent(
            EmailEvent.TYPE_NEWSLETTER_CONFIRM,
            to,
            "Confirm Your Newsletter Subscription",
            "newsletter-subscription-confirmation",
            Map.of("name", name != null ? name : "Subscriber", "confirmationUrl", confirmationUrl)
        );
        queueEmail(event);
    }

    public void queueNewsletterEmail(String to, java.util.List<com.Nguyen.blogplatform.model.Post> posts) {
        EmailEvent event = new EmailEvent(
            EmailEvent.TYPE_NEWSLETTER_POSTS,
            to,
            "Your Newsletter Update",
            "newsletter-posts",
            Map.of("posts", posts)
        );
        queueEmail(event);
    }

    /**
     * Send HTML email directly (not queued).
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("noreply@blog.com");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("HTML email sent successfully to: {}", to);
    }
}
