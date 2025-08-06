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
        
        // Thêm logic cho các placeholders khác ở đây (ví dụ: {{social_links}})

        return processedContent;
    }

    private String generateLatestPostsHtml(User user) {
        // Lấy 5 bài viết mới nhất (cần thêm phương thức này vào PostRepository)
        List<Post> latestPosts = postRepository.findTop5ByUserOrderByCreatedAtDesc(user);

        if (latestPosts.isEmpty()) {
            return "<p><em>Chưa có bài viết nào.</em></p>";
        }

        StringBuilder html = new StringBuilder();
        html.append("<ul>");
        for (Post post : latestPosts) {
            // Lưu ý: Link URL cần được frontend xử lý (ví dụ: /posts/post-slug)
            html.append("<li><a href=\"/posts/")
                .append(post.getSlug())
                .append("\">")
                .append(escapeHtml(post.getTitle())) // Hàm escapeHtml để tránh XSS
                .append("</a></li>");
        }
        html.append("</ul>");

        return html.toString();
    }

    // Hàm helper đơn giản để escape HTML entities
    private String escapeHtml(String text) {
        return text.replace("<", "&lt;").replace(">", "&gt;");
    }
}
