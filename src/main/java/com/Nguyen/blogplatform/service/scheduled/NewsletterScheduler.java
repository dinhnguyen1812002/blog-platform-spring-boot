package com.Nguyen.blogplatform.service.scheduled;



import com.Nguyen.blogplatform.Enum.NewsletterFrequency;
import com.Nguyen.blogplatform.model.Newsletter;
import com.Nguyen.blogplatform.model.Post;

import com.Nguyen.blogplatform.repository.NewsletterRepository;
import com.Nguyen.blogplatform.repository.PostRepository;
import com.Nguyen.blogplatform.service.EmailServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsletterScheduler {

    private final NewsletterRepository newsletterRepository;
    private final PostRepository postRepository;
    private final EmailServices emailServices;

    // chạy mỗi ngày lúc 8h sáng
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyNewsletter() {
        sendNewsletter(NewsletterFrequency.DAILY, 1);
    }

    // chạy mỗi thứ 2
    @Scheduled(cron = "0 0 8 * * MON")
    public void sendWeeklyNewsletter() {
        sendNewsletter(NewsletterFrequency.WEEKLY, 7);
    }

    private void sendNewsletter(NewsletterFrequency frequency, int days) {
        LocalDateTime from = LocalDateTime.now().minusDays(days);

        List<Post> newPosts = postRepository.findByCreatedAtAfter(from);
        if (newPosts.isEmpty()) return;

        List<Newsletter> subscribers =
                newsletterRepository.findByIsActiveTrueAndIsConfirmedTrueAndFrequency(frequency);

        for (Newsletter subscriber : subscribers) {
            try {
                emailServices.sendNewsletterEmail(subscribers, newPosts);
                subscriber.setLastSentAt(LocalDateTime.now());
                newsletterRepository.save(subscriber);
            } catch (Exception e) {
                log.error("Failed sending newsletter to {}", subscriber.getEmail(), e);
            }
        }

        log.info("Sent {} newsletter to {} users", frequency, subscribers.size());
    }
}

