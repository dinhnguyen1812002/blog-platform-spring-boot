# Hướng dẫn Test API cho Tác giả (Author)

Tài liệu này mô tả các API dành riêng cho người dùng có vai trò `ROLE_AUTHOR` để quản lý bài viết của chính họ. Tất cả các API dưới đây đều yêu cầu xác thực.

---

## 1. Lấy danh sách bài viết của bạn

API này trả về danh sách các bài viết do chính tác giả đang đăng nhập tạo ra, có hỗ trợ phân trang.

-   **Method:** `GET`
-   **URL:** `http://localhost:8080/api/v1/author/posts`
-   **Authorization:** Bắt buộc (Token của Author).
-   **Query Params:**
    -   `page`: Số trang (mặc định: `0`).
    -   `size`: Kích thước trang (mặc định: `10`).
-   **Ví dụ:** `http://localhost:8080/api/v1/author/posts?page=0&size=5`

-   **Phản hồi thành công (200 OK):** Trả về một danh sách các bài viết.

---

## 2. Tạo bài viết mới

Cho phép tác giả tạo một bài viết mới. API này chỉ nhận dữ liệu dạng JSON, không bao gồm upload file trực tiếp.

-   **Method:** `POST`
-   **URL:** `http://localhost:8080/api/v1/author/write`
-   **Authorization:** Bắt buộc (Token của Author).
-   **Body:** `raw` - `JSON`

    ```json
    {
      "title": "Tiêu đề bài viết mới",
      "content": "Nội dung chi tiết của bài viết.",
      "categoryId": "uuid-cua-category",
      "tagIds": ["uuid-cua-tag-1", "uuid-cua-tag-2"],
      "thumbnailUrl": "/path/to/image.jpg" // (Tùy chọn) 
    }
    ```
    *Lưu ý: `thumbnailUrl` là đường dẫn tới ảnh đã được upload từ trước (ví dụ: qua API Upload).* 

-   **Phản hồi thành công (200 OK):**

    ```json
    {
        "message": "Post created successfully"
    }
    ```

---

## 3. Cập nhật bài viết

Cho phép tác giả cập nhật một bài viết đã tồn tại của mình.

-   **Method:** `PUT`
-   **URL:** `http://localhost:8080/api/v1/author/{id}`
-   **Authorization:** Bắt buộc (Token của Author và phải là chủ sở hữu bài viết).
-   **Path Variable:**
    -   `id`: ID của bài viết cần cập nhật.
-   **Body:** `form-data`
    -   **Key 1:** `image` (không bắt buộc)
        -   **Value:** Chọn một file ảnh mới để thay thế ảnh đại diện.
    -   **Key 2:** `postRequest`
        -   **Value:** Một chuỗi JSON chứa nội dung cần cập nhật.
            ```json
            {
              "title": "Tiêu đề đã được cập nhật",
              "content": "Nội dung mới cho bài viết."
            }
            ```

-   **Phản hồi thành công (200 OK):** Trả về chi tiết bài viết sau khi đã cập nhật.

---

## 4. Xóa bài viết

Cho phép tác giả xóa một bài viết của mình.

-   **Method:** `DELETE`
-   **URL:** `http://localhost:8080/api/v1/author/{id}`
-   **Authorization:** Bắt buộc (Token của Author và phải là chủ sở hữu bài viết).
-   **Path Variable:**
    -   `id`: ID của bài viết cần xóa.

-   **Phản hồi thành công (204 No Content):** Không có nội dung trả về.
