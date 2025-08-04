# Hướng dẫn API Luồng video (Video Stream)

Tài liệu này mô tả các API để quản lý và truyền phát video.

---

## 1. Tải lên video

- **Method:** `POST`
- **URL:** `http://localhost:8080/video/upload`
- **Body:** `form-data`
    -   **Key:** `file`
    -   **Value:** Chọn một hoặc nhiều tệp video từ máy của bạn.

- **Phản hồi thành công (200 OK):** Trả về một danh sách các đối tượng video đã được tải lên.

---

## 2. Xóa video

- **Method:** `DELETE`
- **URL:** `http://localhost:8080/video/{id}`
- **Path Variable:** `id` - ID của video cần xóa.

- **Phản hồi thành công (204 No Content):** Không có nội dung trả về.

---

## 3. Truyền phát video

- **Method:** `GET`
- **URL:** `http://localhost:8080/video/stream/{id}`
- **Path Variable:** `id` - ID của video cần truyền phát.
- **Headers:**
    -   `Range`: `bytes=start-end` (tùy chọn)

- **Phản hồi thành công (200 OK hoặc 206 Partial Content):** Trả về một phần của nội dung video.
