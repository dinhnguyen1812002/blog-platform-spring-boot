# Hướng dẫn API Bài viết đã lưu (Saved Posts)

Tài liệu này mô tả các API để quản lý các bài viết đã lưu của người dùng.

---

## 1. Lưu một bài viết

- **Method:** `POST`
- **URL:** `http://localhost:8080/api/v1/saved-posts/{postId}`
- **Authorization:** Bắt buộc.
- **Path Variable:** `postId` - ID của bài viết cần lưu.
- **Body:** `raw` - `JSON` (tùy chọn)

    ```json
    {
      "notes": "Ghi chú cho bài viết đã lưu."
    }
    ```

- **Phản hồi thành công (200 OK):**

    ```json
    {
        "message": "Post saved successfully"
    }
    ```

---

## 2. Bỏ lưu một bài viết

- **Method:** `DELETE`
- **URL:** `http://localhost:8080/api/v1/saved-posts/{postId}`
- **Authorization:** Bắt buộc.
- **Path Variable:** `postId` - ID của bài viết cần bỏ lưu.

- **Phản hồi thành công (200 OK):**

    ```json
    {
        "message": "Post unsaved successfully"
    }
    ```

---

## 3. Lấy danh sách bài viết đã lưu

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/v1/saved-posts`
- **Authorization:** Bắt buộc.
- **Query Params:**
    -   `page`: Số trang (mặc định: `0`).
    -   `size`: Kích thước trang (mặc định: `10`).

- **Phản hồi thành công (200 OK):** Trả về một danh sách các bài viết đã lưu.

---

## 4. Lấy danh sách tất cả các bài viết đã lưu

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/v1/saved-posts/list`
- **Authorization:** Bắt buộc.

- **Phản hồi thành công (200 OK):** Trả về một danh sách đầy đủ các bài viết đã lưu.

---

## 5. Kiểm tra xem một bài viết đã được lưu chưa

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/v1/saved-posts/check/{postId}`
- **Authorization:** Bắt buộc.
- **Path Variable:** `postId` - ID của bài viết cần kiểm tra.

- **Phản hồi thành công (200 OK):** Trả về `true` nếu bài viết đã được lưu, ngược lại trả về `false`.

---

## 6. Lấy số lượng bài viết đã lưu

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/v1/saved-posts/count`
- **Authorization:** Bắt buộc.

- **Phản hồi thành công (200 OK):** Trả về tổng số bài viết đã lưu.

---

## 7. Lấy số lượng người dùng đã lưu một bài viết

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/v1/saved-posts/post/{postId}/count`
- **Authorization:** Bắt buộc.
- **Path Variable:** `postId` - ID của bài viết.

- **Phản hồi thành công (200 OK):** Trả về tổng số người dùng đã lưu bài viết.

---

## 8. Cập nhật ghi chú cho một bài viết đã lưu

- **Method:** `PUT`
- **URL:** `http://localhost:8080/api/v1/saved-posts/notes/{savedPostId}`
- **Authorization:** Bắt buộc.
- **Path Variable:** `savedPostId` - ID của bài viết đã lưu.
- **Body:** `raw` - `JSON`

    ```json
    {
      "notes": "Ghi chú đã được cập nhật."
    }
    ```

- **Phản hồi thành công (200 OK):**

    ```json
    {
        "message": "Notes updated successfully"
    }
    ```
