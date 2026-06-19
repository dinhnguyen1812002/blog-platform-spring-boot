# OAuth2 Authentication (Google, GitHub, Discord)

Tài liệu hướng dẫn Frontend tích hợp đăng nhập/đăng ký bằng OAuth2.

## 1. Luồng redirect (khuyến nghị)

1. Frontend điều hướng người dùng đến endpoint khởi tạo OAuth2 của Backend.
2. Backend chuyển hướng đến trang đăng nhập của Provider.
3. Provider gửi authorization code về Backend.
4. Backend tạo/cập nhật User, phát hành JWT + refresh token.
5. Backend redirect về Frontend kèm token trên URL và cookie.

### Endpoint khởi tạo

| Provider | URL |
| :--- | :--- |
| Google | `{BASE_URL}/oauth2/authorization/google` |
| GitHub | `{BASE_URL}/oauth2/authorization/github` |
| Discord | `{BASE_URL}/oauth2/authorization/discord` |

### Redirect sau khi thành công

`{FRONTEND_URL}/oauth2/redirect?token={JWT_TOKEN}`

Backend đồng thời set cookie:

- `token`: JWT access token
- `refresh-token`: refresh token

### Redirect khi lỗi

`{FRONTEND_URL}/oauth2/redirect?error={message}`

---

## 2. Luồng token API (SPA tự xử lý OAuth)

Dùng khi Frontend tự đổi code lấy access token từ Provider, rồi gửi token cho Backend.

### POST `/api/v1/oauth/verify`

Xác minh access token và lấy profile từ Provider.

```json
{
  "provider": "google",
  "accessToken": "ya29...."
}
```

### POST `/api/v1/oauth/login`

Đăng ký/đăng nhập, trả về JWT và set cookie giống `/api/v1/auth/login`.

```json
{
  "provider": "github",
  "accessToken": "gho_...."
}
```

`provider` hỗ trợ: `google`, `github`, `discord`.

---

## 3. Cấu hình Provider

### Google

- Redirect URI: `http://localhost:8080/login/oauth2/code/google`
- Scopes: `email`, `profile`

### GitHub

- Redirect URI: `http://localhost:8080/login/oauth2/code/github`
- Scopes: `read:user`, `user:email`

### Discord

- Redirect URI: `http://localhost:8080/login/oauth2/code/discord`
- Scopes: `identify`, `email`

Biến môi trường (xem `.env.example`):

- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
- `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`
- `DISCORD_CLIENT_ID`, `DISCORD_CLIENT_SECRET`

---

## 4. Ghi chú bảo mật

- Email chưa tồn tại → tự động tạo tài khoản mới với role `ROLE_USER`.
- Email đã tồn tại → liên kết provider và cập nhật avatar nếu có.
- Mật khẩu OAuth được tạo ngẫu nhiên; người dùng có thể đặt lại mật khẩu để đăng nhập bằng email/password.
