# Hướng dẫn Test API Bài viết (Posts)

## 1. Lấy danh sách bài viết

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/v1/post`
- **Authorization:** Không bắt buộc (API công khai).
- **Query Params:**
    -   `page`: Số trang (mặc định: `0`).
    -   `size`: Kích thước trang (mặc định: `5`).

## 2. Lấy bài viết theo slug

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/v1/post/{slug}`
- **Authorization:** Không bắt buộc đối với bài viết công khai (PUBLISHED). Yêu cầu (Token hợp lệ) đối với bài viết PRIVATE.
- **Path Variable:** `slug` - slug của bài viết.
- **Quy tắc bảo mật (Privacy Rules):**
  - **PUBLISHED**: Công khai cho tất cả.
  - **SCHEDULED**: Trả về lỗi `404 Not Found` nếu chưa đến thời gian hẹn giờ xuất bản (`scheduledPublishAt`). Trở thành công khai (200 OK) nếu đã qua thời gian hẹn giờ.
  - **PRIVATE**: Trả về lỗi `403 Forbidden` nếu người gọi API không phải tác giả gốc của bài viết.
  - **DRAFT**: Trả về lỗi `404 Not Found`.

## 3. Lấy bài viết mới nhất

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/v1/post/latest`
- **Authorization:** Không bắt buộc (API công khai).
- **Query Params:**
    -   `limit`: Số lượng bài viết cần lấy (mặc định: `5`).

## 4. Lấy bài viết nổi bật (Featured)

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/v1/post/featured`
- **Authorization:** Không bắt buộc (API công khai).

## 5. Lấy bài viết theo danh mục

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/v1/post/category/{categoryId}`
- **Authorization:** Không bắt buộc (API công khai).
- **Path Variable:** `categoryId` - ID của danh mục.

## 6. Tìm kiếm bài viết

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/v1/post/search`
- **Authorization:** Không bắt buộc (API công khai).
- **Query Params:**
    -   `title`: Tiêu đề bài viết.
    -   `categoryId`: ID của danh mục.

## 7. Like một bài viết

-   **Method:** `POST`
-   **URL:** `http://localhost:8888/api/v1/post/{id}/like`
-   **Authorization:** Bắt buộc.
-   **Path Variable:** `id` - ID của bài viết bạn muốn like.

## 8. Đánh giá (rating) một bài viết

-   **Method:** `POST`
-   **URL:** `http://localhost:8888/api/v1/post/{id}/rate`
-   **Authorization:** Bắt buộc.
-   **Path Variable:** `id` - ID của bài viết.
-   **Query Params:**
-   `score`: Điểm số (từ 1 đến 5).
