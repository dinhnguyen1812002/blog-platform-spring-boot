# Hướng dẫn Tính năng Cập nhật Profile Người dùng

Tài liệu này cung cấp hướng dẫn toàn diện về cách sử dụng và tích hợp API cập nhật thông tin cá nhân của người dùng, bao gồm cả việc tùy chỉnh giao diện profile bằng Markdown và sử dụng các placeholder động.

---

## 1. Tổng quan

Tính năng này cho phép người dùng cập nhật các thông tin cơ bản và tùy chỉnh trang profile công khai của họ bằng cú pháp Markdown. Điều này mang lại sự linh hoạt cho người dùng để thể hiện bản thân, chia sẻ thông tin, và thậm chí nhúng các nội dung động như danh sách bài viết mới nhất.

## 2. API Cập nhật Profile

### Endpoint Cập nhật Profile Markdown

Endpoint này cho phép người dùng đã xác thực cập nhật nội dung Markdown cho trang cá nhân của họ.

- **URL:** `/api/v1/users/profile/custom`
- **Method:** `PUT`
- **Authentication:** Bắt buộc. Cần có Bearer Token hợp lệ trong header `Authorization`.

### Request Body

Request body phải là một đối tượng JSON chứa nội dung Markdown.

| Trường | Kiểu | Mô tả | Bắt buộc |
| :--- | :--- | :--- | :--- |
| `markdownContent` | String | Nội dung Markdown cho profile của người dùng. | Có |

**Ví dụ Request:**

```json
{
  "markdownContent": "# Chào mừng đến với trang của tôi!\n\nTôi là một lập trình viên đam mê các dự án mã nguồn mở.\n\n- **Ngôn ngữ:** Java, Python, JavaScript\n- **Frameworks:** Spring Boot, React"
}
```

### Success Response

- **Code:** `200 OK`
- **Content:** Trả về đối tượng `UserProfileResponse` hoàn chỉnh của người dùng, bao gồm cả trường `customProfileMarkdown` đã được cập nhật.

**Ví dụ Response:**

```json
{
  "id": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
  "username": "john.doe",
  "email": "john.doe@example.com",
  "avatar": "/path/to/avatar.png",
  "postCount": 15,
  "commentCount": 42,
  "customProfileMarkdown": "# Chào mừng đến với trang của tôi!\n\nTôi là một lập trình viên đam mê các dự án mã nguồn mở.\n\n- **Ngôn ngữ:** Java, Python, JavaScript\n- **Frameworks:** Spring Boot, React"
}
```

### Error Responses

- **Code:** `400 Bad Request`
    -   **Lý do:** Request body không hợp lệ (ví dụ: `markdownContent` vượt quá giới hạn ký tự).
    -   **Content:** `{"message": "Nội dung profile không được vượt quá 10000 ký tự"}`
- **Code:** `401 Unauthorized`
    -   **Lý do:** Request không có header `Authorization` hợp lệ hoặc token đã hết hạn.

---

## 3. Xử lý trên Frontend và Vấn đề Bảo mật

### Hiển thị Nội dung Markdown

Phía backend chỉ lưu trữ và trả về chuỗi Markdown thô. **Trách nhiệm quan trọng của ứng dụng frontend là phải xử lý và làm sạch (sanitize) HTML** được render từ Markdown để tránh các lỗ hổng bảo mật.

### **Cảnh báo An ninh: Ngăn chặn Cross-Site Scripting (XSS)**

Việc không làm sạch HTML trước khi hiển thị sẽ khiến ứng dụng của bạn bị tấn công XSS. Chúng tôi thực sự khuyên bạn nên sử dụng một thư viện như **`DOMPurify`** ở phía client sau khi chuyển đổi Markdown sang HTML.

**Ví dụ logic ở Frontend (JavaScript):**

```javascript
import { marked } from 'marked';
import DOMPurify from 'dompurify';

// 1. Lấy nội dung Markdown thô từ API
const rawMarkdown = userProfile.customProfileMarkdown;

// 2. Chuyển Markdown thành HTML (bước này có thể tạo ra HTML không an toàn)
const unsafeHtml = marked.parse(rawMarkdown);

// 3. Làm sạch HTML để loại bỏ các script hoặc thẻ độc hại
const safeHtml = DOMPurify.sanitize(unsafeHtml);

// 4. Chỉ sau khi đã làm sạch, mới render HTML ra trang
document.getElementById('profile-display-area').innerHTML = safeHtml;
```

---

## 4. Sử dụng Placeholder Động

Để làm cho profile sinh động hơn, hệ thống hỗ trợ các "placeholder" đặc biệt trong Markdown. Backend sẽ tự động tìm và thay thế các placeholder này bằng dữ liệu thực tế của người dùng trước khi trả về cho client.

### Các Placeholder được hỗ trợ

| Placeholder | Mô tả |
| :--- | :--- |
| `{{latest_posts}}` | Hiển thị danh sách 5 bài viết mới nhất của người dùng dưới dạng danh sách HTML. |
| `{{post_count}}` | Hiển thị tổng số bài viết của người dùng. |
| `{{social_links}}` | (Ví dụ) Hiển thị danh sách các liên kết mạng xã hội. |
| `{{user_bio}}` | (Ví dụ) Hiển thị tiểu sử của người dùng. |

### Cách hoạt động

1.  Người dùng chèn placeholder vào trong trình soạn thảo Markdown.
    
    **Ví dụ:**
    
    ```markdown
    ### Bài viết mới nhất của tôi
    
    {{latest_posts}}
    
    ---
    
    **Tổng số bài viết:** {{post_count}}
    ```
    
2.  Khi một client khác yêu cầu xem profile này, backend sẽ xử lý nội dung Markdown:
    -   Tìm `{{latest_posts}}` và thay thế bằng một đoạn mã HTML `<ul>...</ul>` chứa danh sách các bài viết.
    -   Tìm `{{post_count}}` và thay thế bằng số lượng bài viết thực tế.
3.  API trả về một chuỗi đã được xử lý, là sự kết hợp giữa Markdown của người dùng và HTML do backend tạo ra.
4.  Frontend vẫn phải **làm sạch (sanitize)** toàn bộ nội dung này như đã mô tả ở **Mục 3** để đảm bảo an toàn. Thư viện `marked` thường sẽ giữ nguyên các thẻ HTML hợp lệ trong quá trình render.

Bằng cách này, người dùng có thể tạo ra các trang profile phong phú và cập nhật tự động mà không cần can thiệp thủ công.
