# Hướng dẫn Xác thực (Authentication)

Hầu hết các API trong hệ thống đều yêu cầu xác thực bằng JWT (JSON Web Token). Để lấy token, bạn cần thực hiện 2 bước: đăng ký tài khoản và đăng nhập.

## 1. Đăng ký tài khoản (`/api/v1/auth/register`)

- **Method:** `POST`
- **URL:** `http://localhost:8080/api/v1/auth/register`
- **Body:** `raw` - `JSON`

```json
{
  "username": "testuser",
  "email": "testuser@example.com",
  "password": "password123",
  "role": ["ROLE_USER"]
}
```

**Phản hồi (Response):**

```json
{
    "message": "User registered successfully!"
}
```

## 2. Đăng nhập để lấy Token (`/api/v1/auth/login`)

- **Method:** `POST`
- **URL:** `http://localhost:8080/api/v1/auth/login`
- **Body:** `raw` - `JSON`

```json
{
  "email": "testuser@example.com",
  "password": "password123"
}
```

**Phản hồi (Response):**

```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY3OT...",
    "id": "...",
    "username": "testuser",
    "email": "testuser@example.com",
    "roles": [
        "ROLE_USER"
    ]
}
```

**Lưu lại `token`:** Sao chép giá trị của trường `token` từ phản hồi. Bạn sẽ cần dùng nó cho các yêu cầu (request) cần xác thực.

## 3. Lấy thông tin người dùng hiện tại (`/api/v1/auth/me`)

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/v1/auth/me`
- **Headers:**
    - `Authorization`: `Bearer <your_jwt_token>`

**Phản hồi (Response):**

```json
{
    "id": "...",
    "username": "testuser",
    "role": [
        "ROLE_USER"
    ]
}
```

## 4. Đăng xuất (`/api/v1/auth/logout`)

- **Method:** `POST`
- **URL:** `http://localhost:8080/api/v1/auth/logout`
- **Headers:**
    - `Authorization`: `Bearer <your_jwt_token>`

**Phản hồi (Response):**

```json
{
    "message": "You've been signed out!"
}
```

## 5. Cách gửi yêu cầu đã xác thực

Với mỗi yêu cầu API cần xác thực, bạn cần thêm `token` vào Header.

- **Key:** `Authorization`
- **Value:** `Bearer <your_jwt_token>`