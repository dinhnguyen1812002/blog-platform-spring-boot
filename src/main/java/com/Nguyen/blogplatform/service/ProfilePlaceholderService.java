package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfilePlaceholderService {

    private final PostRepository postRepository;

    public String processPlaceholders(String rawMarkdown, User user) {
        if (rawMarkdown == null || rawMarkdown.isBlank()) {
            return "";
        }

        String processedContent = rawMarkdown;

        if (processedContent.contains("{{latest_posts}}")) {
            processedContent = processedContent.replace("{{latest_posts}}", generateLatestPostsHtml(user));
        }

        if (processedContent.contains("{{post_count}}")) {
            processedContent = processedContent.replace("{{post_count}}", String.valueOf(postRepository.countByUser(user)));
        }
        
        if (processedContent.contains("{{user_bio}}")) {
            String bio = user.getBio();
            processedContent = processedContent.replace("{{user_bio}}", bio != null ? bio : "<em>No bio available.</em>");
        }
        
        if (processedContent.contains("{{social_links}}")) {
            processedContent = processedContent.replace("{{social_links}}", generateSocialLinksHtml(user));
        }

        return processedContent;
    }

    private String generateLatestPostsHtml(User user) {
        // Get 5 latest posts
        List<Post> latestPosts = postRepository.findTop5ByUserOrderByCreatedAtDesc(user);

        if (latestPosts.isEmpty()) {
            return "<p><em>No posts available.</em></p>";
        }

        StringBuilder html = new StringBuilder();
        html.append("<ul class=\"latest-posts-list\">");
        for (Post post : latestPosts) {
            html.append("<li><a href=\"/posts/")
                .append(post.getSlug())
                .append("\">")
                .append(escapeHtml(post.getTitle()))
                .append("</a></li>");
        }
        html.append("</ul>");

        return html.toString();
    }
    
    private String generateSocialLinksHtml(User user) {
        if (user.getSocialMediaLinks() == null || user.getSocialMediaLinks().isEmpty()) {
            return "<p><em>No social media links available.</em></p>";
        }
        
        StringBuilder html = new StringBuilder();
        html.append("<ul class=\"social-links-list\">");
        
        user.getSocialMediaLinks().forEach(link -> {
            if (link.getUrl() != null && !link.getUrl().isBlank()) {
                html.append("<li><a href=\"")
                    .append(escapeHtml(link.getUrl()))
                    .append("\" target=\"_blank\">")
                    .append(escapeHtml(link.getPlatform().toString()))
                    .append("</a></li>");
            }
        });
        
        html.append("</ul>");
        
        return html.toString();
    }

    // Helper method to escape HTML entities
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("<", "&lt;").replace(">", "&gt;");
    }
}