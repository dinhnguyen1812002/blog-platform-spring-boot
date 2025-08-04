# Hướng dẫn API Danh mục (Category)

Tài liệu này mô tả các API để quản lý danh mục bài viết.

---

## 1. Lấy tất cả danh mục

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/v1/category`
- **Authorization:** Bắt buộc (Token của User).

- **Phản hồi thành công (200 OK):** Trả về một danh sách các danh mục.

---

## 2. Lấy danh mục theo ID

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/v1/category/{id}`
- **Path Variable:**
    -   `id`: ID của danh mục cần lấy.

- **Phản hồi thành công (200 OK):** Trả về chi tiết danh mục.

---

## 3. Tạo danh mục mới

- **Method:** `POST`
- **URL:** `http://localhost:8080/api/v1/category/add`
- **Body:** `raw` - `JSON`

    ```json
    {
      "category": "Tên danh mục",
      "description": "Mô tả danh mục.",
      "backgroundColor": "#FFFFFF"
    }
    ```

- **Phản hồi thành công (200 OK):**

    ```json
    {
        "message": "Category created successfully"
    }
    ```

---

## 4. Cập nhật danh mục

- **Method:** `PUT`
- **URL:** `http://localhost:8080/api/v1/category/{id}`
- **Path Variable:**
    -   `id`: ID của danh mục cần cập nhật.
- **Body:** `raw` - `JSON`

    ```json
    {
      "category": "Tên danh mục đã cập nhật",
      "description": "Mô tả mới cho danh mục.",
      "backgroundColor": "#000000"
    }
    ```

- **Phản hồi thành công (200 OK):** Trả về chi tiết danh mục sau khi đã cập nhật.

---

## 5. Xóa danh mục

- **Method:** `DELETE`
- **URL:** `http://localhost:8080/api/v1/category/{id}`
- **Path Variable:**
    -   `id`: ID của danh mục cần xóa.

- **Phản hồi thành công (200 OK):** Trả về chi tiết danh mục đã bị xóa.
