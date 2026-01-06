package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.model.Newsletter;
import com.Nguyen.blogplatform.model.Post;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
public class EmailServices {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${base-url}")
    private String baseUrl;

    @Value("${frontend-url}")
    private String frontendUrl;

    public EmailServices(JavaMailSender mailSender,
                         TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /* ================= PASSWORD RESET ================= */

    public void sendPasswordResetEmail(String to, String resetUrl)
            throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Password Reset Request");
        helper.setText("""
                <p>Hello,</p>
                <p>You requested to reset your password.</p>
                <p><a href="%s">Reset Password</a></p>
                <br>
                <p>If you didn’t request this, please ignore.</p>
                """.formatted(resetUrl), true);

        mailSender.send(message);
    }

    /* ================= NEWSLETTER CONFIRMATION ================= */

    public void sendNewsletterConfirmationEmail(
            String to,
            String name,
            String confirmationToken
    ) throws MessagingException {

        String confirmationUrl =
                baseUrl + "/api/v1/newsletter/confirm?token=" + confirmationToken;

        Context context = new Context();
        context.setVariable("name", name != null ? name : "Subscriber");
        context.setVariable("confirmationUrl", confirmationUrl);

        String html = templateEngine.process(
                "newsletter-subscription-confirmation",
                context
        );

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("noreply@blog.com");
        helper.setTo(to);
        helper.setSubject("Confirm Your Newsletter Subscription");
        helper.setText(html, true);

        mailSender.send(message);
    }

    /* ================= NEWSLETTER (DAILY / WEEKLY) ================= */

    public void sendNewsletterEmail(
            List<Newsletter> subscribers,
            List<Post> posts
    ) throws MessagingException {

        if (posts == null || posts.isEmpty() || subscribers.isEmpty()) {
            return;
        }

        for (Newsletter subscriber : subscribers) {
            sendNewsletterEmailToSubscriber(subscriber, posts);
        }
    }

    /* ================= PRIVATE HELPER ================= */

    private void sendNewsletterEmailToSubscriber(
            Newsletter subscriber,
            List<Post> posts
    ) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        String unsubscribeUrl =
                baseUrl + "/api/v1/newsletter/unsubscribe?token="
                        + subscriber.getSubscriptionToken();

        Context context = new Context();
        context.setVariable(
                "subscriberName",
                subscriber.getName() != null
                        ? subscriber.getName()
                        : "Subscriber"
        );
        context.setVariable("posts", posts);
        context.setVariable("frontendUrl", frontendUrl);
        context.setVariable("unsubscribeUrl", unsubscribeUrl);

        String htmlContent =
                templateEngine.process("newsletter-email", context);

        helper.setFrom("noreply@blog.com");
        helper.setTo(subscriber.getEmail());
        helper.setSubject("📰 New articles from our blog");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}
