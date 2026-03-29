# OAuth2 Authentication Documentation (Google, GitHub, Discord)

Tài liệu này hướng dẫn Frontend cách tích hợp tính năng đăng nhập bằng mạng xã hội (OAuth2) với Backend.

## 1. Luồng hoạt động (Flow)

1. **Khởi tạo**: Frontend điều hướng người dùng đến URL khởi tạo OAuth2 của Backend.
2. **Xác thực**: Backend chuyển hướng người dùng đến trang đăng nhập của Provider (Google/GitHub/Discord).
3. **Ủy quyền**: Sau khi người dùng đồng ý, Provider gửi mã code về Backend.
4. **Xử lý**: Backend xác thực với Provider, tạo/cập nhật User trong Database và tạo JWT Token.
5. **Kết thúc**: Backend chuyển hướng người dùng quay lại Frontend kèm theo Token trên URL và Cookie.

---

## 2. Các Endpoint khởi tạo (Backend)

Frontend chỉ cần đặt các link sau vào nút "Login with...":

| Provider | Endpoint Khởi tạo (GET) |
| :--- | :--- |
| **Google** | `http://localhost:8080/oauth2/authorization/google` |
| **GitHub** | `http://localhost:8080/oauth2/authorization/github` |
| **Discord** | `http://localhost:8080/oauth2/authorization/discord` |

---

## 3. Xử lý sau khi đăng nhập thành công

Sau khi xử lý xong, Backend sẽ redirect người dùng về địa chỉ:
`{FRONTEND_URL}/oauth2/redirect?token={JWT_TOKEN}`

**Ví dụ thực tế:**
`http://localhost:5173/oauth2/redirect?token=eyJhbGciOiJIUzI1NiJ9...`

### Đồng thời Backend cũng trả về các Cookie:
* `token`: Chứa JWT Access Token.
* `refresh-token`: Chứa Refresh Token.

---

## 4. Hướng dẫn cho Frontend

### Bước 1: Tạo Route xử lý Redirect
Frontend cần tạo một route ví dụ `/oauth2/redirect`. Component xử lý route này nên thực hiện:

1. Lấy `token` từ Query Parameter trên URL.
2. Lưu `token` vào `localStorage` hoặc `Context/Redux`.
3. Kiểm tra thông tin User hiện tại (gọi API `/api/v1/auth/me`).
4. Chuyển hướng người dùng vào trang chủ (Dashboard/Home).

**Mẫu code xử lý (React):**
```javascript
import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';

const OAuth2RedirectHandler = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    useEffect(() => {
        const token = searchParams.get('token');

        if (token) {
            localStorage.setItem('token', token);
            // Có thể gọi thêm API fetch thông tin user tại đây
            navigate('/');
        } else {
            navigate('/login?error=oauth2_failed');
        }
    }, [searchParams, navigate]);

    return <div>Đang xử lý đăng nhập...</div>;
};
```

---

## 5. Xử lý lỗi

Nếu có lỗi xảy ra trong quá trình xác thực, Backend sẽ redirect về:
`{FRONTEND_URL}/oauth2/redirect?error={message}`

Frontend nên hiển thị thông báo lỗi phù hợp cho người dùng.

---

## 6. Lưu ý bảo mật
* Backend đã được cấu hình để tự động tạo tài khoản nếu email chưa tồn tại.
* Mật khẩu cho các tài khoản OAuth2 được tạo ngẫu nhiên và bảo mật cao, người dùng có thể đặt lại mật khẩu sau nếu muốn đăng nhập bằng Username/Password truyền thống.
