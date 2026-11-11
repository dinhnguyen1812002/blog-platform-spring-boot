## Xác thực trình duyệt bằng cookie

Backend đặt access token và refresh token trong cookie HttpOnly; frontend không cần lưu hoặc gửi JWT thủ công. Với mọi request, dùng `credentials: "include"` (Axios: `withCredentials: true`). Access token hết hạn theo `JWT_EXPIRATION`; dùng `POST /api/v1/auth/refresh-token` để làm mới.

Trước các request POST, PUT, PATCH hoặc DELETE, gọi `GET /api/v1/auth/csrf` với credentials. Giữ session cookie và gửi trường `token` trong header có tên trả về ở `headerName` (thường là `X-CSRF-TOKEN`). Lấy token trước login và lấy lại khi session thay đổi hoặc hết hạn. Bearer/API key không kèm cookie vẫn được hỗ trợ mà không cần CSRF token.

```js
const base = "http://localhost:8080";
const csrf = await fetch(`${base}/api/v1/auth/csrf`, { credentials: "include" }).then(r => r.json());
await fetch(`${base}/api/v1/auth/login`, {
  method: "POST", credentials: "include",
  headers: { "Content-Type": "application/json", [csrf.headerName]: csrf.token },
  body: JSON.stringify({ email: "user@example.com", password: "password" })
});
```

Cookie được cấu hình SameSite=Lax; frontend và backend cần cùng site. Production dùng HTTPS và `cookieSecure=true`. CORS phải cho phép chính xác origin của frontend. Nếu frontend và backend ở hai site khác nhau, cần cấu hình cookie và CSRF phù hợp trước khi triển khai.

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