# Hướng dẫn API cho tác giả

Hướng dẫn này cung cấp hướng dẫn chi tiết để tác giả quản lý bài viết của mình, bao gồm tạo, cập nhật, xóa và lấy bài viết.

## 1. Tạo bài viết mới

Endpoint này cho phép một tác giả đã được xác thực tạo một bài viết blog mới.

### Endpoint

`POST /api/v1/author/write`

### Quy trình làm việc

1.  **(Tùy chọn) Tải lên ảnh đại diện (Thumbnail):** Nếu bài viết có ảnh đại diện, client trước tiên phải tải tệp ảnh lên endpoint `/api/v1/upload`. Thao tác này sẽ trả về một URL cho ảnh đã tải lên.
2.  **Tạo bài viết:** Sau đó, client gọi endpoint `/api/v1/author/write` này, bao gồm URL của ảnh đại diện (nếu có) trong phần thân yêu cầu.

### Yêu cầu (Request)

#### Headers

| Header        | Giá trị             | Mô tả                               | 
| ------------- | ------------------ | -----------------------------------------
| `Authorization` | `Bearer <JWT_TOKEN>` | JWT token để xác thực người dùng. |
| `Content-Type`  | `application/json` | Kiểu nội dung của phần thân yêu cầu.     |

#### Body

Phần thân yêu cầu phải là một đối tượng JSON với các thuộc tính sau:

```json
{
  "title": "Tiêu đề bài viết của bạn",
  "content": "Nội dung đầy đủ của bài viết của bạn.",
  "thumbnail": "URL_cua_anh_dai_dien",
  "categories": ["Công nghệ", "Java"],
  "tags": ["SpringBoot", "API"],
  "featured": false,
  "public_date": "2025-09-01T10:00:00"
}
```

#### Tham số Body

| Tham số    | Kiểu      | Bắt buộc | Mô tả                                                                                               |
| ------------ | --------- | -------- | -------------------------------------------------------------------------------------------------------- |
| `title`      | `String`  | Có      | Tiêu đề của bài viết.                                                                                    |
| `content`    | `String`  | Có      | Nội dung chính của bài viết, có thể ở định dạng HTML hoặc Markdown.                                          |
| `thumbnail`  | `String`  | Không       | URL của ảnh đại diện của bài viết. URL này nên được lấy từ endpoint `/api/v1/upload`.        |
| `categories` | `List<String>` | Không       | Một danh sách tên danh mục để liên kết với bài viết.                                                      |
| `tags`       | `List<String>` | Không       | Một danh sách tên thẻ để liên kết với bài viết.                                                           |
| `featured`   | `boolean` | Không       | Liệu bài viết có nên được đánh dấu là "nổi bật" hay không. Mặc định là `false`.                                     |
| `public_date`| `String`  | Không       | Ngày và giờ bài viết sẽ được xuất bản, ở định dạng ISO-8601 (ví dụ: `2025-09-01T10:00:00`). Nếu không được cung cấp, bài viết sẽ được xuất bản ngay lập tức. |

### Phản hồi (Responses)

#### Phản hồi thành công

- **Mã trạng thái:** `201 Created`
- **Nội dung:** Một đối tượng JSON với thông báo thành công.

```json
{
  "message": "Post created successfully"
}
```

#### Phản hồi lỗi

- **Mã trạng thái:** `400 Bad Request`
  - **Nội dung:** Nếu phần thân yêu cầu không hợp lệ (ví dụ: thiếu `title` hoặc `content`).
  ```json
  {
    "message": "Post title is required"
  }
  ```

- **Mã trạng thái:** `401 Unauthorized`
  - **Nội dung:** Nếu người dùng chưa được xác thực hoặc JWT token không hợp lệ.
  ```json
  {
    "message": "User not authenticated or invalid authentication type"
  }
  ```

- **Mã trạng thái:** `500 Internal Server Error`
  - **Nội dung:** Nếu xảy ra lỗi không mong muốn trên máy chủ.
  ```json
  {
    "message": "Error creating post: <error_details>"
  }
  ```

### Ví dụ cURL

```bash
curl -X POST http://localhost:8080/api/v1/author/write \
-H "Authorization: Bearer <your_jwt_token>" \
-H "Content-Type: application/json" \
-d '{
      "title": "My First Post", 
      "content": "<h1>Hello World!</h1><p>This is my first post.</p>", 
      "thumbnail": "http://example.com/uploads/my-thumbnail.jpg", 
      "categories": ["Introduction"], 
      "tags": ["welcome", "first-post"], 
      "public_date": "2025-09-01T10:00:00" 
    }'
```

## 2. Get Author's Posts

-   **Method:** `GET`
-   **URL:** `http://localhost:8888/api/v1/author/posts`
-   **Authorization:** Required.
-   **Query Parameters:**
    -   `page` (integer, optional, default: 0): The page number to retrieve.
    -   `size` (integer, optional, default: 10): The number of items per page.

## 3. Update a Post

-   **Method:** `PUT`
-   **URL:** `http://localhost:8888/api/v1/author/{id}`
-   **Authorization:** Required.
-   **Path Variable:**
    -   `id` (string): The unique identifier of the post to update.
-   **Body:** `raw` - `JSON` (Same as create post)

## 4. Delete a Post

-   **Method:** `DELETE`
-   **URL:** `http://localhost:8888/api/v1/author/{postId}`
-   **Authorization:** Required.
-   **Path Variable:**
    -   `postId` (string): The unique identifier of the post to delete.

## 5. Get Post Detail

-   **Method:** `GET`
-   **URL:** `http://localhost:8888/api/v1/author/{postId}`
-   **Authorization:** Required.
-   **Path Variable:**
    -   `postId` (string): The unique identifier of the post.
