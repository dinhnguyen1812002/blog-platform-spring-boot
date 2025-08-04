# Hướng dẫn Test API Quản lý Tài khoản

## 1. Lấy thông tin người dùng (`/api/v1/users/me`)

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/v1/users/me`
- **Authorization:** **Bắt buộc**.

## 2. Cập nhật mật khẩu

-   **Method:** `POST`
-   **URL:** `http://localhost:8080/api/v1/users/update-password`
-   **Authorization:** Bắt buộc.
-   **Body:** `raw` - `JSON`

```json
{
  "oldPassword": "password123",
  "newPassword": "newPassword456"
}
```

## 3. Quên mật khẩu

-   **Method:** `POST`
-   **URL:** `http://localhost:8080/api/v1/auth/forgot-password`
-   **Authorization:** Không bắt buộc.
-   **Body:** `raw` - `JSON`

```json
{
  "email": "testuser@example.com"
}
```
(Hệ thống sẽ gửi một email chứa token để reset mật khẩu).

## 4. Đặt lại mật khẩu

-   **Method:** `POST`
-   **URL:** `http://localhost:8080/api/v1/auth/reset-password`
-   **Authorization:** Không bắt buộc.
-   **Body:** `raw` - `JSON`

```json
{
  "token": "token-nhan-duoc-tu-email",
  "newPassword": "newStrongPassword789"
}
```
