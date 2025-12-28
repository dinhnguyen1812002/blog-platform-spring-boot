package com.Nguyen.blogplatform.service.scheduled;

import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.payload.response.notification.PublicArticleNotification;
import com.Nguyen.blogplatform.repository.PostRepository;
import com.Nguyen.blogplatform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledPublishService {

    private final PostRepository postRepository;
    private final NotificationService notificationService;

    /**
     * Scheduled task to check and publish posts that are due to be published
     * Runs every 30 seconds
     */
    @Scheduled(fixedRate = 30000)
    public void checkForPostPublishing() {
        try {
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
            post.setIs_publish(true);
            Post publishedPost = postRepository.save(post);

            log.info("Post published: {} (ID: {})", publishedPost.getTitle(), publishedPost.getId());

            // Create notification payload
            PublicArticleNotification notification = new PublicArticleNotification(
                    publishedPost.getId(),
                    publishedPost.getTitle(),
                    publishedPost.getThumbnail(),
                    publishedPost.getExcerpt(),
                    publishedPost.getSlug(),
                    publishedPost.getPublic_date()
            );

            // Send notification to the author
            notificationService.sendPostPublishedNotification(
                    publishedPost.getUser().getUsername(),
                    notification
            );

            // Broadcast to all connected users
            notificationService.broadcastArticlePublishedNotification(notification);

            // Create database notification record
            notificationService.createUserNotification(
                    publishedPost.getUser().getId(),
                    "POST_PUBLISHED",
                    "Article Published",
                    "Your article '" + publishedPost.getTitle() + "' has been published successfully!"
            );

        } catch (Exception e) {
            log.error("Error publishing post with ID: {}", post.getId(), e);
        }
    }
}
