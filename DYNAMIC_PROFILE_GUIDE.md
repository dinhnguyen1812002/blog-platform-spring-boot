# Hướng dẫn Tích hợp Nội dung Động vào Profile Markdown

Để cho phép người dùng hiển thị thông tin động như danh sách bài viết mới nhất, link mạng xã hội, hoặc bio, chúng ta sẽ triển khai một hệ thống xử lý **placeholder** ở phía backend.

Người dùng sẽ chèn các thẻ giữ chỗ đặc biệt vào trong file Markdown của họ. Trước khi nội dung này được trả về cho client, backend sẽ tìm và thay thế các thẻ này bằng dữ liệu HTML tương ứng.

---

## Kế hoạch triển khai

### 1. Định nghĩa các Placeholders

Chúng ta cần quyết định một danh sách các placeholder được hỗ trợ. Cú pháp `{{placeholder_name}}` là một lựa chọn phổ biến.

**Ví dụ về các placeholders:**

- `{{latest_posts}}`: Hiển thị danh sách 5 bài viết mới nhất của người dùng.
- `{{social_links}}`: Hiển thị danh sách các link mạng xã hội mà người dùng đã cấu hình.
- `{{post_count}}`: Hiển thị tổng số bài viết của người dùng.
- `{{user_bio}}`: Hiển thị phần tiểu sử ngắn của người dùng (nếu có trường này trong model).
- `{{top_rated_posts}}`: Hiển thị 5 bài viết có đánh giá cao nhất.

### 2. Tạo một Service xử lý Placeholder

Việc xử lý logic nên được tách ra một service riêng để giữ cho `UserProfileService` gọn gàng.

**Tạo file mới: `src/main/java/com/Nguyen/blogplatform/service/ProfilePlaceholderService.java`**

```java
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
```

**Cập nhật `PostRepository`:**

Thêm phương thức để lấy các bài viết mới nhất của một user cụ thể.

**File: `src/main/java/com/Nguyen/blogplatform/repository/PostRepository.java`**
```java
// ... imports
public interface PostRepository extends JpaRepository<Post, String>, JpaSpecificationExecutor<Post> {
    // ... các phương thức hiện có

    List<Post> findTop5ByUserOrderByCreatedAtDesc(User user);
}
```

### 3. Tích hợp vào `UserProfileService`

Bây giờ, hãy gọi service mới này từ `UserProfileService` trước khi trả về response.

**File: `src/main/java/com/Nguyen/blogplatform/service/UserProfileService.java`**

```java
// ... imports

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final ProfilePlaceholderService placeholderService; // Inject service mới

    // ... các phương thức khác

    public UserProfileResponse getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));
        
        // Lấy markdown gốc từ user
        String rawMarkdown = user.getCustomProfileMarkdown();

        // Xử lý placeholder để tạo nội dung đã được xử lý
        String processedContent = placeholderService.processPlaceholders(rawMarkdown, user);

        // Trả về cả hai hoặc chỉ nội dung đã xử lý tùy theo yêu cầu của bạn
        return mapToUserProfileResponse(user, processedContent);
    }

    // Cập nhật phương thức mapping
    private UserProfileResponse mapToUserProfileResponse(User user, String processedMarkdown) {
        return new UserProfileResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getAvatar(),
            // ... các trường khác
            processedMarkdown // Trả về nội dung đã được xử lý
        );
    }
    
    // Phương thức cập nhật profile không thay đổi nhiều
    @Transactional
    public UserProfileResponse updateUserProfileMarkdown(String userId, String markdownContent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        
        // ... validation quyền

        // Lưu nội dung Markdown GỐC, chưa qua xử lý
        user.setCustomProfileMarkdown(markdownContent);
        User updatedUser = userRepository.save(user);

        // Xử lý placeholder trước khi trả về response
        String processedContent = placeholderService.processPlaceholders(updatedUser.getCustomProfileMarkdown(), updatedUser);

        return mapToUserProfileResponse(updatedUser, processedContent);
    }
}
```

### 4. Lưu ý cho Frontend

- **Sanitization vẫn là bắt buộc!** Mặc dù backend đã chèn HTML, người dùng vẫn có thể tự viết Markdown chứa mã độc. Frontend **vẫn phải** dùng `DOMPurify` hoặc một công cụ tương tự để lọc HTML cuối cùng trước khi hiển thị.
- Frontend nhận về một chuỗi đã là sự kết hợp giữa Markdown của người dùng và HTML do backend tạo ra. Thư viện render Markdown (như `marked`) thường sẽ bỏ qua các thẻ HTML hợp lệ, nên việc hiển thị sẽ diễn ra đúng như mong đợi.

### Kết luận

Bằng cách này, bạn đã tạo ra một hệ thống linh hoạt cho phép người dùng nhúng các thành phần động vào trang cá nhân của họ mà không cần họ phải biết code phức tạp, đồng thời vẫn giữ quyền kiểm soát và xử lý dữ liệu ở phía backend.

```