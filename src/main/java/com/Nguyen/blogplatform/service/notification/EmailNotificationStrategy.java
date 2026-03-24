package com.Nguyen.blogplatform.service.notification;

import com.Nguyen.blogplatform.Enum.EDeliveryChannel;
import com.Nguyen.blogplatform.Enum.EDeliveryStatus;
import com.Nguyen.blogplatform.model.NotificationHistory;
import com.Nguyen.blogplatform.model.NotificationTemplate;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.model.UserNotificationPreferences;
import com.Nguyen.blogplatform.repository.UserNotificationPreferencesRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationStrategy implements NotificationStrategy {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final UserNotificationPreferencesRepository preferencesRepository;
    private final UserRepository userRepository;

    @Value("${spring.mail.from:noreply@blogplatform.com}")
    private String fromEmail;

    @Override
    public String getChannel() {
        return EDeliveryChannel.EMAIL.name();
    }

    @Override
    public NotificationHistory send(NotificationHistory history, NotificationTemplate template, Map<String, Object> templateData) {
        try {
            User user = userRepository.findById(history.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + history.getUserId()));

            String emailAddress = getEmailAddress(history.getUserId(), user);
            if (emailAddress == null || emailAddress.isEmpty()) {
                throw new RuntimeException("No email address available for user: " + history.getUserId());
            }

            String subject = processTemplate(template.getSubject(), templateData);
            String htmlContent = processTemplate(template.getEmailHtmlTemplate(), templateData);
            String textContent = processTemplate(template.getEmailTextTemplate(), templateData);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(emailAddress);
            helper.setSubject(subject);

            if (htmlContent != null && !htmlContent.isEmpty()) {
                helper.setText(textContent != null ? textContent : htmlContent, htmlContent);
            } else {
                helper.setText(textContent != null ? textContent : template.getContent(), false);
            }

            mailSender.send(message);

            history.setStatus(EDeliveryStatus.SENT);
            history.setSentAt(LocalDateTime.now());
            history.setRecipientAddress(emailAddress);
            history.setSubject(subject);

            log.info("Email notification sent to: {}", emailAddress);

        } catch (MessagingException e) {
            log.error("Failed to send email notification", e);
            history.setStatus(EDeliveryStatus.FAILED);
            history.setErrorMessage("Email sending failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending email notification", e);
            history.setStatus(EDeliveryStatus.FAILED);
            history.setErrorMessage(e.getMessage());
        }

        return history;
    }

    @Override
    public boolean isAvailable(String userId) {
        return preferencesRepository.findByUserIdAndChannel(userId, EDeliveryChannel.EMAIL)
            .map(pref -> pref.getEnabled() != null && pref.getEnabled())
            .orElse(true);
    }

    private String getEmailAddress(String userId, User user) {
        return preferencesRepository.findByUserIdAndChannel(userId, EDeliveryChannel.EMAIL)
            .map(UserNotificationPreferences::getEmailAddress)
            .filter(email -> email != null && !email.isEmpty())
            .orElse(user.getEmail());
    }

    private String processTemplate(String template, Map<String, Object> data) {
        if (template == null || data == null || data.isEmpty()) {
            return template;
        }

        try {
            Context context = new Context();
            context.setVariables(data);
            return templateEngine.process(template, context);
        } catch (Exception e) {
            log.warn("Template processing failed, using simple replacement", e);
            return simpleTemplateReplace(template, data);
        }
    }

    private String simpleTemplateReplace(String template, Map<String, Object> data) {
        if (template == null) return null;

        String result = template;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }
}
