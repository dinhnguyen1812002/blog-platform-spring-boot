# Kế hoạch Xây dựng Frontend cho Blog Platform


    # Axios để gọi API
    bun install axios

    # TanStack Query (React Query) để quản lý state từ server (fetching, caching)
    bun install @tanstack/react-query

    # Zustand để quản lý state global của client (nhẹ nhàng hơn Redux)
    bun install zustand

    # Thư viện cho form (tùy chọn nhưng khuyến khích)
    bun install react-hook-form @hookform/resolvers zod
    ```


2 **Thiết lập Axios Instance:**
    -   Tạo một instance Axios trong `src/config/axios.ts` với `baseURL` là `http://localhost:8080/api/v1` và `withCredentials: true` để trình duyệt tự động gửi cookie chứa token.

---

## Giai đoạn 1: Layout & Giao diện người dùng cốt lõi

Mục tiêu: Xây dựng bộ khung giao diện và các component cơ bản.

1.  **Tạo `MainLayout`:**
    -   Một layout chung bao gồm `Header` và `Footer`.
    -   `Header` chứa logo, các link điều hướng chính, nút Đăng nhập/Đăng ký hoặc avatar người dùng.
    -   `Footer` chứa thông tin bản quyền, liên kết.

2.  **Xây dựng các Component UI cơ bản (`src/components/ui`):**
    -   `Button`: Nút bấm với các biến thể (primary, secondary, danger).
    -   `Input`: Ô nhập liệu.
    -   `Card`: Khung thẻ để hiển thị bài viết, meme.
    -   `Spinner`: Icon loading.
    -   `Modal`: Cửa sổ popup.
    -   `Avatar`: Hiển thị ảnh đại diện người dùng.

---

## Giai đoạn 2: Xác thực người dùng

Mục tiêu: Hoàn thiện luồng đăng ký, đăng nhập, đăng xuất.

1.  **Tạo `AuthStore` (Zustand):**
    -   Store để lưu trữ thông tin người dùng (id, username, email, roles) và trạng thái đã đăng nhập hay chưa.

2.  **Tạo các trang:**
    -   `LoginPage.tsx`: Form đăng nhập.
    -   `RegisterPage.tsx`: Form đăng ký.

3.  **Xử lý Logic:**
    -   Gọi API login/register từ các trang tương ứng.
    -   Khi đăng nhập thành công, lưu thông tin user vào `AuthStore`.
    -   Khi logout, xóa thông tin user khỏi store.

4.  **Tạo `ProtectedRoute`:**
    -   Một component bậc cao (HOC) hoặc một layout riêng để kiểm tra xem người dùng đã đăng nhập và có đủ quyền (`role`) để truy cập một trang hay chưa. Nếu không, chuyển hướng về trang đăng nhập.

---

## Giai đoạn 3: Triển khai các tính năng chính

Mục tiêu: Xây dựng các trang và chức năng cốt lõi của ứng dụng.

1.  **Trang chủ (`/`):**
    -   Hiển thị danh sách các bài viết nổi bật (`/posts/featured`).
    -   Hiển thị danh sách các bài viết mới nhất (`/posts` có phân trang).

2.  **Trang chi tiết bài viết (`/posts/:slug`):**
    -   Hiển thị nội dung bài viết, thông tin tác giả, ngày đăng.
    -   Hiển thị các nút tương tác: Like, Rating.
    -   Hiển thị khu vực bình luận (`/posts/:postId/comments`).
    -   Form để người dùng đã đăng nhập có thể thêm bình luận mới.

3.  **Trang của Tác giả (Author - Yêu cầu đăng nhập & `ROLE_AUTHOR`):**
    -   **Dashboard (`/author/posts`):** Hiển thị danh sách các bài viết của chính tác giả, với các nút Sửa/Xóa.
    -   **Trang viết bài mới (`/author/posts/new`):** Form để tạo bài viết mới, bao gồm trình soạn thảo văn bản (có thể dùng thư viện `tiptap`), ô chọn Category, Tag, và upload ảnh thumbnail.
    -   **Trang sửa bài viết (`/author/posts/edit/:postId`):** Tương tự trang viết bài mới nhưng load dữ liệu cũ.

4.  **Trang quản lý của Admin (Yêu cầu đăng nhập & `ROLE_ADMIN`):**
    -   **Dashboard (`/admin`):** Cung cấp các liên kết đến các trang quản lý khác.
    -   **Quản lý Category (`/admin/categories`):** Giao diện CRUD (Tạo, Đọc, Cập nhật, Xóa) cho các danh mục.
    -   **Quản lý Tag (`/admin/tags`):** Giao diện CRUD cho các thẻ.

5.  **Các trang khác:**
    -   **Trang Meme (`/memes`):** Hiển thị danh sách các meme theo kiểu masonry layout (lưới không đều).
    -   **Trang bài viết đã lưu (`/bookmarks` - Yêu cầu đăng nhập):** Hiển thị các bài viết người dùng đã lưu.

---

## Giai đoạn 4: Hoàn thiện & Tối ưu

Mục tiêu: Cải thiện trải nghiệm người dùng và chuẩn bị cho deployment.

1.  **Xử lý trạng thái Loading:**
    -   Hiển thị `Spinner` hoặc skeleton loader (giao diện giả) trong khi chờ dữ liệu từ API.

2.  **Xử lý lỗi:**
    -   Hiển thị thông báo lỗi thân thiện khi API gặp sự cố.

3.  **Tối ưu cho di động (Responsive):**
    -   Sử dụng các breakpoint của Tailwind (`sm:`, `md:`, `lg:`) để đảm bảo giao diện hiển thị tốt trên mọi kích thước màn hình.

4.  **Deployment:**
    -   Chạy `npm run build` để tạo bản build production.
    -   Triển khai thư mục `dist` lên một nền tảng hosting tĩnh như **Vercel** hoặc **Netlify** để có hiệu suất tốt nhất và dễ dàng thiết lập CI/CD.
