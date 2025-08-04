# Hướng dẫn API Thẻ (Tags)

Tài liệu này mô tả các API để quản lý các thẻ.

---

## 1. Tạo một thẻ mới

- **Method:** `POST`
- **URL:** `http://localhost:8080/api/tags`
- **Body:** `raw` - `JSON`

    ```json
    {
      "name": "Tên thẻ",
      "description": "Mô tả thẻ.",
      "color": "#FFFFFF"
    }
    ```

- **Phản hồi thành công (200 OK):**

    ```json
    {
        "message": "Tag created successfully"
    }
    ```

---

## 2. Lấy tất cả các thẻ

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/tags`

- **Phản hồi thành công (200 OK):** Trả về một danh sách các thẻ.

---

## 3. Lấy thẻ theo ID

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/tags/{id}`
- **Path Variable:** `id` - ID của thẻ.

- **Phản hồi thành công (200 OK):** Trả về chi tiết của thẻ.

---

## 4. Cập nhật một thẻ

- **Method:** `PUT`
- **URL:** `http://localhost:8080/api/tags/{id}`
- **Path Variable:** `id` - ID của thẻ.
- **Body:** `raw` - `JSON`

    ```json
    {
      "name": "Tên thẻ đã được cập nhật",
      "description": "Mô tả mới cho thẻ.",
      "color": "#000000"
    }
    ```

- **Phản hồi thành công (200 OK):**

    ```json
    {
        "message": "Tag updated successfully"
    }
    ```

---

## 5. Xóa một thẻ

- **Method:** `DELETE`
- **URL:** `http://localhost:8080/api/tags/{id}`
- **Path Variable:** `id` - ID của thẻ.

- **Phản hồi thành công (200 OK):**

    ```json
    {
        "message": "Tag deleted successfully"
    }
    ```

---

## 6. Lấy các thẻ mới nhất

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/tags/latest`

- **Phản hồi thành công (200 OK):** Trả về một danh sách các thẻ mới nhất.
