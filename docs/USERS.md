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

---

## 5. Cập nhật thông tin Profile người dùng

- **Method:** `PUT`
- **URL:** `http://localhost:8080/api/v1/user/profile`
- **Authorization:** Bắt buộc (Bearer Token).
- **Body:** `raw` - `JSON`

- **Request Body Fields:**
    - `username` (String): Tên người dùng mới. Phải có ít nhất 3 ký tự.
    - `email` (String): Địa chỉ email mới. Phải là một email hợp lệ.
    - `avatar` (String): URL đến ảnh đại diện mới.
    - `bio` (String): Giới thiệu ngắn về bản thân.
    - `website` (String): URL đến trang web cá nhân.
    - `socialMediaLinks` (Map<String, String>): Một đối tượng chứa các liên kết mạng xã hội.
        - `key`: Nền tảng mạng xã hội (ví dụ: `FACEBOOK`, `TWITTER`, `LINKEDIN`, `GITHUB`).
        - `value`: URL đến trang cá nhân trên nền tảng đó.

- **Ví dụ Request Body:**
    ```json
    {
      "username": "newusername",
      "email": "new.email@example.com",
      "avatar": "https://example.com/new_avatar.png",
      "bio": "Đây là bio mới của tôi.",
      "website": "https://my-new-website.com",
      "socialMediaLinks": {
        "TWITTER": "https://twitter.com/new_handle",
        "GITHUB": "https://github.com/new_username"
      }
    }
    ```

- **Phản hồi thành công (200 OK):**
    - Trả về đối tượng `UserProfileResponse` với thông tin đã được cập nhật.
