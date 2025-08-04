# Các tính năng đã được triển khai

Dưới đây là danh sách các tính năng đã được triển khai trong dự án nền tảng blog này, được suy ra từ việc phân tích mã nguồn và các tệp tài liệu hiện có.

## I. Quản lý người dùng và xác thực

- **Đăng ký người dùng:** Người dùng mới có thể đăng ký tài khoản.
- **Đăng nhập/Đăng xuất:** Người dùng có thể đăng nhập bằng email và mật khẩu, và đăng xuất.
- **Xác thực dựa trên JWT:** Hệ thống sử dụng JSON Web Tokens (JWT) để xác thực, bao gồm cả vai trò của người dùng trong token.
- **Quản lý vai trò:**
    - API để quản lý vai trò (ADMIN, AUTHOR, USER).
    - Gán và xóa vai trò cho người dùng.
    - Thống kê số lượng người dùng theo vai trò.
- **Hồ sơ người dùng:**
    - Lấy thông tin hồ sơ của người dùng hiện tại (`/api/v1/auth/me`).
    - Lấy thông tin hồ sơ công khai của người dùng khác.
    - Thống kê số lượng bài đăng, bài đăng đã lưu và bình luận của người dùng.
- **Đặt lại mật khẩu:** Người dùng có thể yêu cầu đặt lại mật khẩu qua email.
- **Cập nhật mật khẩu:** Người dùng đã đăng nhập có thể thay đổi mật khẩu của họ.

## II. Quản lý bài đăng (Post)

- **Tạo, đọc, cập nhật, xóa (CRUD) bài đăng:** Các tác giả có thể quản lý các bài đăng của riêng họ.
- **Lấy danh sách bài đăng:**
    - Lấy danh sách tất cả các bài đăng với phân trang.
    - Lấy các bài đăng mới nhất.
    - Lấy các bài đăng nổi bật.
- **Chi tiết bài đăng:** Lấy chi tiết một bài đăng bằng ID hoặc slug.
- **Tìm kiếm và lọc:**
    - Tìm kiếm bài đăng theo tiêu đề.
    - Lọc bài đăng theo danh mục.
- **Tương tác với bài đăng:**
    - **Thích:** Người dùng có thể thích và bỏ thích một bài đăng.
    - **Đánh giá:** Người dùng có thể đánh giá một bài đăng (ví dụ: từ 1 đến 5 sao).
- **Đếm lượt xem:** Tự động tăng số lượt xem khi một bài đăng được truy cập.

## III. Quản lý nội dung khác

- **Danh mục (Category):**
    - CRUD cho các danh mục bài đăng.
    - Gán bài đăng vào các danh mục.
- **Thẻ (Tag):**
    - CRUD cho các thẻ.
    - Gán thẻ cho bài đăng.
- **Bình luận (Comment):**
    - Người dùng có thể bình luận về các bài đăng.
    - Hỗ trợ các bình luận lồng nhau (trả lời bình luận).
    - Lấy danh sách bình luận của một bài đăng với phân trang.
- **Bài đăng đã lưu (Bookmark):**
    - Người dùng có thể lưu và bỏ lưu các bài đăng để đọc sau.
    - Lấy danh sách các bài đăng đã lưu của người dùng.
    - Thêm ghi chú vào các bài đăng đã lưu.

## IV. Các tính năng bổ sung

- **Bản tin (Newsletter):**
    - Người dùng có thể đăng ký nhận bản tin qua email.
    - Yêu cầu xác nhận đăng ký qua email.
    - API quản trị để xem và quản lý những người đã đăng ký.
- **Tải lên tệp:**
    - Tải lên hình ảnh cho các bài đăng.
    - Tải lên video.
- **Phát video (Video Streaming):**
    - API để phát video đã tải lên, hỗ trợ HTTP Range requests để tua video.
- **Meme:**
    - Tải lên và hiển thị meme.
    - API để lấy meme ngẫu nhiên.
- **Tài liệu API:** Sử dụng Swagger để tạo tài liệu API tương tác.
- **Gỡ lỗi (Debug):** Các endpoint đặc biệt để gỡ lỗi các vấn đề liên quan đến vai trò và dữ liệu người dùng.

## V. Kiến trúc và công nghệ

- **Spring Boot:** Nền tảng chính của ứng dụng.
- **Spring Security:** Xử lý xác thực và ủy quyền.
- **Spring Data JPA & Hibernate:** Tương tác với cơ sở dữ liệu.
- **MySQL:** Cơ sở dữ liệu quan hệ.
- **Thymeleaf:** Được sử dụng cho các mẫu email và một số trang giao diện đơn giản.
- **Lombok:** Giảm mã soạn sẵn trong các mô hình Java.
- **Gradle:** Công cụ xây dựng dự án.
