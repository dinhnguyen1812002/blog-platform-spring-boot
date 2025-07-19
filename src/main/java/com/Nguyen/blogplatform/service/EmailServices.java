package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.model.Newsletter;
import com.Nguyen.blogplatform.model.Post;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
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

    public EmailServices(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendPasswordResetEmail(String to, String resetUrl) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        String subject = "Password Reset Request";
        String content = "<p>Hello,</p>"
                + "<p>You have requested to reset your password.</p>"
                + "<p>Click the link below to reset your password:</p>"
                + "<p><a href=\"" + resetUrl + "\">Reset Password</a></p>"
                + "<br>"
                + "<p>If you did not request a password reset, please ignore this email or contact support if you have questions.</p>"
                + "<p>Thank you,<br>The Team</p>";

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(message);
    }

    public void sendNewsletterConfirmationEmail(String to, String name, String confirmationToken) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        String confirmationUrl = baseUrl + "/api/v1/newsletter/confirm?token=" + confirmationToken;

        Context context = new Context();
        context.setVariable("name", name != null ? name : "Subscriber");
        context.setVariable("confirmationUrl", confirmationUrl);

        String htmlContent = templateEngine.process("newsletter-subscription-confirmation", context);

        helper.setFrom("noreply@Blog.com");
        helper.setTo(to);
        helper.setSubject("Confirm Your Newsletter Subscription");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    public void sendNewsletterEmail(List<Newsletter> subscribers, Post post) throws MessagingException {
        for (Newsletter subscriber : subscribers) {
            sendNewsletterEmailToSubscriber(subscriber, post);
        }
    }

    private void sendNewsletterEmailToSubscriber(Newsletter subscriber, Post post) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        String unsubscribeUrl = baseUrl + "/api/v1/newsletter/unsubscribe?token=" + subscriber.getSubscriptionToken();
        String postUrl = baseUrl + "/post/" + post.getSlug();

        Context context = new Context();
        context.setVariable("subscriberName", subscriber.getName() != null ? subscriber.getName() : "Subscriber");
        context.setVariable("postTitle", post.getTitle());
        context.setVariable("postContent", post.getContent().length() > 200 ?
                post.getContent().substring(0, 200) + "..." : post.getContent());
        context.setVariable("postUrl", postUrl);
        context.setVariable("unsubscribeUrl", unsubscribeUrl);
        context.setVariable("postThumbnail", post.getThumbnail());
        context.setVariable("authorName", post.getUser().getUsername());

        String htmlContent = templateEngine.process("newsletter-email", context);

        helper.setFrom("noreply@Blog.com");
        helper.setTo(subscriber.getEmail());
        helper.setSubject("New Article: " + post.getTitle());
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}