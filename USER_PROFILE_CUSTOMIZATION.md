# Hướng dẫn triển khai tính năng Custom User Profile bằng Markdown

Tính năng này cho phép người dùng tự soạn thảo và tùy chỉnh trang cá nhân của họ bằng cú pháp Markdown.

## Kế hoạch triển khai

### Bước 1: Cập nhật Database và Model

Chúng ta cần thêm một trường mới vào `User` model để lưu nội dung Markdown mà người dùng nhập vào.

**File: `src/main/java/com/Nguyen/blogplatform/model/User.java`**

Thêm trường `customProfileMarkdown` vào class `User`:

```java
// ... imports

@Entity
@Table(name = "user")
@Data
public class User {
    
    // ... các trường hiện có (id, username, email, etc.)

    @Lob // Sử dụng @Lob để lưu trữ một chuỗi văn bản dài
    @Column(name = "custom_profile_markdown", columnDefinition = "TEXT")
    private String customProfileMarkdown;

    // ... constructors, getters, setters
}
```

- `@Lob`: Annotation này chỉ định rằng trường nên được lưu trữ dưới dạng một đối tượng lớn (Large Object) trong cơ sở dữ liệu.
- `columnDefinition = "TEXT"`: Đảm bảo cột trong cơ sở dữ liệu có kiểu dữ liệu phù hợp để lưu trữ văn bản dài.

### Bước 2: Cập nhật DTO (Data Transfer Object)

**1. Cập nhật `UserProfileResponse`:**

Thêm trường mới vào `UserProfileResponse` để khi client gọi API lấy thông tin user, nội dung markdown sẽ được trả về.

**File: `src/main/java/com/Nguyen/blogplatform/payload/response/UserProfileResponse.java`**

```java
// ... imports and annotations
public class UserProfileResponse {
    // ... các trường hiện có

    private String customProfileMarkdown;

    // ... constructor, getters, setters
    // Cập nhật constructor để bao gồm trường mới
    public UserProfileResponse(String id, String username, String email, String avatar, /* các trường khác */, String customProfileMarkdown) {
        // ... gán các giá trị
        this.customProfileMarkdown = customProfileMarkdown;
    }
}
```

**2. Tạo `CustomProfileRequest`:**

Tạo một DTO mới để nhận dữ liệu từ client khi họ cập nhật profile.

**Tạo file mới: `src/main/java/com/Nguyen/blogplatform/payload/request/CustomProfileRequest.java`**

```java
package com.Nguyen.blogplatform.payload.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomProfileRequest {

    @Size(max = 10000, message = "Nội dung profile không được vượt quá 10000 ký tự")
    private String markdownContent;
}
```

### Bước 3: Cập nhật Service Layer

Bây giờ, chúng ta cần thêm logic vào `UserProfileService` để xử lý việc cập nhật và lấy dữ liệu profile.

**File: `src/main/java/com/Nguyen/blogplatform/service/UserProfileService.java`**

```java
// ... imports

@Service
@RequiredArgsConstructor // Hoặc sử dụng @Autowired
public class UserProfileService {

    private final UserRepository userRepository;
    // ... các dependency khác

    // Thêm phương thức mới để cập nhật custom profile
    @Transactional
    public UserProfileResponse updateUserProfileMarkdown(String userId, String markdownContent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        // Kiểm tra quyền hạn (chỉ user đó mới được cập nhật profile của chính họ)
        UserDetailsImpl currentUser = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!currentUser.getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to update this profile.");
        }

        user.setCustomProfileMarkdown(markdownContent);
        User updatedUser = userRepository.save(user);

        return mapToUserProfileResponse(updatedUser); // Giả sử bạn có một phương thức mapping
    }
    
    // Đảm bảo phương thức lấy profile hiện tại trả về trường mới
    public UserProfileResponse getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));
        return mapToUserProfileResponse(user);
    }

    // Helper method để map User entity sang UserProfileResponse
    private UserProfileResponse mapToUserProfileResponse(User user) {
        // ... logic mapping các trường khác
        return new UserProfileResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getAvatar(),
            // ... các trường khác
            user.getCustomProfileMarkdown() // Thêm trường mới
        );
    }
}
```

### Bước 4: Cập nhật Controller (API Endpoint)

Tạo một endpoint mới trong `UserController` để người dùng có thể gửi yêu cầu `PUT` để cập nhật profile của họ.

**File: `src/main/java/com/Nguyen/blogplatform/controller/UserController.java`**

```java
// ... imports

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserProfileService userProfileService;

    // ... các endpoint hiện có

    @PutMapping("/profile/custom")
    @PreAuthorize("isAuthenticated()") // Đảm bảo chỉ user đã đăng nhập mới có thể gọi
    public ResponseEntity<UserProfileResponse> updateCustomProfile(
            @Valid @RequestBody CustomProfileRequest request) {
        
        UserDetailsImpl currentUser = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserProfileResponse updatedProfile = userProfileService.updateUserProfileMarkdown(currentUser.getId(), request.getMarkdownContent());
        
        return ResponseEntity.ok(updatedProfile);
    }
}
```

### Bước 5: Vấn đề Bảo mật (Rất quan trọng!)

**Cross-Site Scripting (XSS):** Vì bạn cho phép người dùng nhập nội dung tùy chỉnh (sẽ được render thành HTML từ Markdown), bạn phải **cực kỳ cẩn thận** với các cuộc tấn công XSS. Kẻ tấn công có thể chèn các thẻ `<script>` độc hại vào profile của họ.

**Giải pháp:**
- **Phía Backend:** Không bắt buộc, nhưng có thể cân nhắc việc lọc bỏ các thẻ HTML nguy hiểm trước khi lưu vào database.
- **Phía Frontend (Bắt buộc):** Đây là nơi quan trọng nhất để xử lý. Khi render nội dung Markdown ra HTML, **bắt buộc phải sử dụng một thư viện có chức năng lọc (sanitize) HTML output.**
    - **Ví dụ cho JavaScript:** Sử dụng thư viện như `DOMPurify` kết hợp với một trình render Markdown như `marked` hoặc `react-markdown`.
    
    ```javascript
    import { marked } from 'marked';
    import DOMPurify from 'dompurify';

    const rawMarkdown = userProfile.customProfileMarkdown;
    const unsafeHtml = marked.parse(rawMarkdown);
    const safeHtml = DOMPurify.sanitize(unsafeHtml);

    // Bây giờ mới hiển thị `safeHtml` trên trang
    document.getElementById('profile-content').innerHTML = safeHtml;
    ```

### Bước 6: Hướng dẫn cho Frontend (Tổng quan)

1.  **Tạo trang chỉnh sửa profile:** Thêm một ô `textarea` lớn để người dùng có thể nhập nội dung Markdown.
2.  **Thêm tính năng Preview:** Cung cấp một tab "Xem trước" để người dùng thấy nội dung của họ sẽ trông như thế nào sau khi render.
3.  **Gọi API:** Khi người dùng nhấn "Lưu", gọi API `PUT /api/v1/users/profile/custom` với body là `{ "markdownContent": "nội dung từ textarea" }`.
4.  **Hiển thị Profile:** Trên trang cá nhân của người dùng, lấy dữ liệu user, đọc trường `customProfileMarkdown` và sử dụng một thư viện an toàn (như đã đề cập ở Bước 5) để render nó ra HTML.