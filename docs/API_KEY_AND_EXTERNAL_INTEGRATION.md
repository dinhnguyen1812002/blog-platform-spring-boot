# API Key Management & External Integration Guide

Tài liệu này hướng dẫn cách sử dụng hệ thống API Key để tích hợp blog của bạn với các nền tảng bên ngoài (ví dụ: hiển thị bài viết lên Portfolio, Mobile App cá nhân, v.v.)

---

## 1. Quản lý API Key (Dành cho User)

Người dùng có thể tạo nhiều cặp API Key/Secret để sử dụng cho các mục đích khác nhau.

### 1.1 Tạo API Key mới
*   **Endpoint:** `POST /api/v1/user/api-keys`
*   **Authentication:** Yêu cầu JWT Token (Đã đăng nhập)
*   **Request Body:**
    ```json
    {
      "name": "My Personal Portfolio"
    }
    ```
*   **Response (201 Created):**
    ```json
    {
      "id": "uuid-string",
      "apiKey": "ak_live_...",
      "apiSecret": "sk_live_...", 
      "name": "My Personal Portfolio",
      "createdAt": "2024-04-05T..."
    }
    ```
    > **LƯU Ý QUAN TRỌNG:** `apiSecret` chỉ hiển thị **DUY NHẤT MỘT LẦN** ngay khi tạo. Hãy lưu trữ nó an toàn.

### 1.2 Danh sách API Key của tôi
*   **Endpoint:** `GET /api/v1/user/api-keys`
*   **Response:** Danh sách các key đang hoạt động (không bao gồm secret).

### 1.3 Thu hồi (Xóa) API Key
*   **Endpoint:** `DELETE /api/v1/user/api-keys/{id}`
*   **Response:** `204 No Content`

---

## 2. Truy xuất bài viết qua API Key (Dành cho External App)

Sau khi có API Key và Secret, bạn có thể gọi API từ bất kỳ môi trường nào (Node.js, Python, React, v.v.) mà không cần qua quy trình đăng nhập JWT phức tạp.

### 2.1 Lấy danh sách bài viết công khai
*   **Endpoint:** `GET /api/v1/external/posts`
*   **Headers:**
    *   `X-API-KEY`: `<Your API Key>`
    *   `X-API-SECRET`: `<Your API Secret>`
*   **Query Parameters (Optional):**
    *   `page`: Số trang (mặc định 0)
    *   `size`: Số lượng mỗi trang (mặc định 20)

### 2.2 Ví dụ gọi API bằng cURL
```bash
curl -X GET "https://api.yourdomain.com/api/v1/external/posts?size=5" \
     -H "X-API-KEY: ak_live_your_key" \
     -H "X-API-SECRET: sk_live_your_secret"
```

### 2.3 Cấu trúc dữ liệu trả về (Response)
Dữ liệu trả về ở dạng phân trang (Spring Page object):
```json
{
  "content": [
    {
      "id": "uuid",
      "title": "Tiêu đề bài viết",
      "slug": "tieu-de-bai-viet",
      "excerpt": "Tóm tắt bài viết...",
      "thumbnail": "https://...",
      "publishedAt": "2024-04-05T...",
      "authorUsername": "nguyen"
    }
  ],
  "totalElements": 10,
  "totalPages": 2,
  "last": false
}
```

---

## 3. Quy tắc bảo mật
1.  **Chỉ trả về bài viết Công khai (PUBLISHED):** Các bài viết nháp (DRAFT) hoặc đã lên lịch sẽ không xuất hiện qua API này.
2.  **Rate Limiting:** API Key bị giới hạn tốc độ gọi giống như người dùng thông thường để tránh tấn công DoS.
3.  **Hết hạn:** Hiện tại API Key không có thời gian hết hạn trừ khi bạn chủ động thu hồi (Revoke).
