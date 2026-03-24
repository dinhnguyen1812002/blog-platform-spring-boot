package com.Nguyen.blogplatform.service.scheduled;

import com.Nguyen.blogplatform.Enum.PublishStatus;
import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.payload.response.notification.PublicArticleNotification;
import com.Nguyen.blogplatform.repository.PostRepository;
import com.Nguyen.blogplatform.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import glide.api.GlideClient;
import glide.api.models.commands.SetOptions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledPublishService {

    private final PostRepository postRepository;
    private final NotificationService notificationService;
    private final GlideClient glideClient;

    private static final String LOCK_KEY = "lock:post_publishing";
    private static final long LOCK_EXPIRY_SECONDS = 30;

    /**
     * Scheduled task to check and publish posts that are due to be published
     * Runs every 30 seconds
     */
    @Scheduled(fixedRate = 30000)
    public void checkForPostPublishing() {
        try {
            // Attempt to acquire distributed lock using Valkey (GlideClient)
            // SET lock:post_publishing "locked" NX EX 30
            String result = glideClient.set(LOCK_KEY, "locked", SetOptions.builder()
                    .conditionalSet(SetOptions.ConditionalSet.ONLY_IF_DOES_NOT_EXIST)
                    .expiry(SetOptions.Expiry.Seconds(LOCK_EXPIRY_SECONDS))
                    .build()).get();

            if (result == null) {
                // Lock already held by another instance
                log.debug("Skip publishing: Lock held by another instance.");
                return;
            }

            log.info("Lock acquired. Checking for posts to publish...");

            LocalDateTime now = LocalDateTime.now();
            List<Post> posts = postRepository.findDueToPublish(now);

            if (posts.isEmpty()) {
                log.debug("No posts due for publishing at {}", now);
                return;
            }

            log.info("Found {} posts due for publishing", posts.size());

            for (Post post : posts) {
                publishPost(post);
            }
        } catch (Exception e) {
            log.error("Error in scheduled post publishing task", e);
        }
    }

    /**
     * Publish a single post and send real-time notifications
     */
    private void publishPost(Post post) {
        try {
            // Update status and published time
            post.setVisibility(PublishStatus.PUBLISHED);
            post.setPublishedAt(LocalDateTime.now());
            post.setScheduledPublishAt(null);
            
            Post publishedPost = postRepository.save(post);

            log.info("Post published: {} (ID: {})", publishedPost.getTitle(), publishedPost.getId());

            // Create notification payload for real-time broadcast
            PublicArticleNotification notification = new PublicArticleNotification(
                    publishedPost.getId(),
                    publishedPost.getTitle(),
                    publishedPost.getThumbnail(),
                    publishedPost.getExcerpt(),
                    publishedPost.getSlug(),
                    publishedPost.getPublishedAt());

            // Broadcast to all connected users
            notificationService.broadcastArticlePublishedNotification(notification);

            // Create database notification record for the author (Private Notification)
            notificationService.createUserNotification(
                    publishedPost.getAuthor().getId(),
                    "POST_PUBLISHED",
                    "Article Published Successfully",
                    "Your scheduled article '" + publishedPost.getTitle() + "' has been published successfully!");

        } catch (Exception e) {
            log.error("Error publishing post with ID: {}", post.getId(), e);
        }
    }
}
