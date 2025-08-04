# Hướng dẫn API Người dùng (Users)

Tài liệu này mô tả các API để quản lý người dùng.

---

## 1. Quên mật khẩu

- **Method:** `POST`
- **URL:** `http://localhost:8080/api/user/forgot-password`
- **Body:** `x-www-form-urlencoded`
    -   **Key:** `email`
    -   **Value:** `địa chỉ email của bạn`

- **Phản hồi thành công (200 OK):**

    ```html
    <!-- Trả về một trang HTML thông báo rằng email đã được gửi. -->
    ```

---

## 2. Đặt lại mật khẩu

- **Method:** `POST`
- **URL:** `http://localhost:8080/api/user/reset-password`
- **Body:** `x-www-form-urlencoded`
    -   **Key 1:** `token`
    -   **Value 1:** `mã thông báo đặt lại mật khẩu của bạn`
    -   **Key 2:** `newPassword`
    -   **Value 2:** `mật khẩu mới của bạn`

- **Phản hồi thành công (200 OK):**

    ```html
    <!-- Trả về một trang HTML thông báo rằng mật khẩu đã được đặt lại. -->
    ```

---

## 3. Cập nhật mật khẩu

- **Method:** `PATCH`
- **URL:** `http://localhost:8080/api/user/update-password`
- **Authorization:** Bắt buộc.
- **Body:** `raw` - `JSON`

    ```json
    {
      "oldPassword": "mật khẩu cũ của bạn",
      "newPassword": "mật khẩu mới của bạn"
    }
    ```

- **Phản hồi thành công (200 OK):**

    ```json
    {
        "message": "Password updated successfully"
    }
    ```

---

## 4. Lấy thông tin người dùng

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/user/{id}`
- **Path Variable:** `id` - ID của người dùng.

- **Phản hồi thành công (200 OK):**

    ```json
    {
        "id": "...",
        "username": "testuser",
        "email": "testuser@example.com",
        "roles": [
            "ROLE_USER"
        ]
    }
    ```
