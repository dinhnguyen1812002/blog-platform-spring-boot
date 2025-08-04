# Hướng dẫn Test API cho Blog Platform

Tài liệu này hướng dẫn cách kiểm thử (test) các API của dự án Blog Platform.

## 1. Chuẩn bị

### Công cụ cần thiết
- **Postman hoặc bất cứ api testing tool nào bạn thích :** Tải tại [https://www.postman.com/downloads/](https://www.postman.com/downloads/).
- **Swagger UI:** Truy cập [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) sau khi chạy ứng dụng.
- **Java & Gradle:** Đảm bảo bạn đã cài đặt Java và Gradle.

### Chạy ứng dụng
```bash
./gradlew bootRun
```
Ứng dụng sẽ chạy trên cổng `8888`.

## 2. Hướng dẫn theo chức năng

Vui lòng tham khảo các tài liệu chi tiết dưới đây cho từng nhóm chức năng:

- **[Xác thực (Authentication)](./AUTHENTICATION.md)**: Đăng ký, đăng nhập, quản lý token.
- **[Quản lý tài khoản (Account Management)](./ACCOUNT_MANAGEMENT.md)**: Lấy thông tin user, đổi mật khẩu, quên mật khẩu.
- **[Bài viết (Posts)](./POSTS.md)**: CRUD bài viết, like, rating, lọc.
- **[Bình luận (Comments)](./COMMENTS.md)**: Thêm và xem bình luận.
- **[Lưu bài viết (Bookmarks)](./BOOKMARKS.md)**: Lưu và xem các bài viết đã lưu.
- **[Upload File](./UPLOADS.md)**: Tải lên hình ảnh.
- **[Meme](./MEMES.md)**: Tạo và xem meme.
- **[Newsletter](./NEWSLETTER.md)**: Đăng ký và hủy đăng ký nhận tin.
- **[Quản lý (Admin)](./ADMIN_MANAGEMENT.md)**: Quản lý Tags và Categories.
- **[Tác giả (Author)](./AUTHOR.md)**: Quản lý bài viết của riêng họ.

## 3. Test bằng `curl`

Nếu bạn không dùng Postman, bạn có thể sử dụng `curl` trên terminal.

### Đăng nhập
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
-H "Content-Type: application/json" \
-d '{
  "username": "testuser",
  "password": "password123"
}'
```

### Lấy danh sách bài viết (sử dụng token)
```bash
# Thay YOUR_TOKEN bằng token bạn nhận được
TOKEN="YOUR_TOKEN"

curl -X GET http://localhost:8080/api/v1/posts \
-H "Authorization: Bearer $TOKEN"
```

```