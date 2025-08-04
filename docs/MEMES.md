# Hướng dẫn Test API Meme

Tài liệu này mô tả các API liên quan đến việc quản lý và truy xuất Meme.

---

## 1. Upload Meme

### 1.1. Upload một Meme

API này cho phép upload một file ảnh kèm theo thông tin (dưới dạng JSON) để tạo thành một meme.

-   **Method:** `POST`
-   **URL:** `http://localhost:8080/api/memes/upload`
-   **Authorization:** Yêu cầu xác thực (tùy theo cấu hình security của bạn).
-   **Body:** `form-data`
    -   **Key 1:** `file`
        -   **Value:** Chọn một file hình ảnh từ máy của bạn.
    -   **Key 2:** `meme`
        -   **Value:** Một chuỗi JSON chứa thông tin của meme. Ví dụ:
            ```json
            {
              "title": "Khi bạn fix được bug lúc nửa đêm",
              "userId": "uuid-cua-user"
            }
            ```

-   **Phản hồi thành công (200 OK):** Trả về chi tiết của meme vừa được tạo.

### 1.2. Upload nhiều Meme

Cho phép upload nhiều file và thông tin tương ứng trong cùng một request.

-   **Method:** `POST`
-   **URL:** `http://localhost:8080/api/memes/upload/multiple`
-   **Authorization:** Yêu cầu xác thực.
-   **Body:** `form-data`
    -   **Key 1:** `file`
        -   **Value:** Chọn nhiều file hình ảnh.
    -   **Key 2:** `memes`
        -   **Value:** Một mảng các đối tượng JSON, mỗi đối tượng tương ứng với một file theo đúng thứ tự. Ví dụ:
            ```json
            [
                {
                    "title": "Meme thứ nhất",
                    "userId": "uuid-user-1"
                },
                {
                    "title": "Meme thứ hai",
                    "userId": "uuid-user-2"
                }
            ]
            ```

-   **Phản hồi thành công (200 OK):** Trả về một danh sách các meme vừa được tạo.

---

## 2. Lấy thông tin Meme

### 2.1. Lấy danh sách Meme (phân trang)

Lấy danh sách các meme trong hệ thống, có phân trang.

-   **Method:** `GET`
-   **URL:** `http://localhost:8080/api/memes`
-   **Authorization:** Không bắt buộc.
-   **Query Param:**
    -   `page`: Số trang (mặc định: `0`).
-   **Ví dụ:** `http://localhost:8080/api/memes?page=1`

-   **Phản hồi thành công (200 OK):** Trả về một đối tượng chứa danh sách meme và thông tin phân trang.

### 2.2. Lấy Meme theo Slug

Lấy thông tin chi tiết của một meme dựa vào `slug` của nó.

-   **Method:** `GET`
-   **URL:** `http://localhost:8080/api/memes/{slug}`
-   **Authorization:** Không bắt buộc.
-   **Path Variable:**
    -   `slug`: Slug của meme (ví dụ: `khi-ban-fix-duoc-bug-luc-nua-dem`).
-   **Ví dụ:** `http://localhost:8080/api/memes/khi-ban-fix-duoc-bug-luc-nua-dem`

-   **Phản hồi thành công (200 OK):** Trả về chi tiết của meme.

---

## 3. Stream Meme ngẫu nhiên (Server-Sent Events)

API này sử dụng công nghệ Server-Sent Events (SSE) để đẩy (push) một meme ngẫu nhiên tới client mỗi 5 phút. Client chỉ cần kết nối một lần và sẽ liên tục nhận được dữ liệu mới.

-   **Method:** `GET`
-   **URL:** `http://localhost:8080/api/memes/random-stream`
-   **Authorization:** Không bắt buộc.
-   **Media Type:** `text/event-stream`

**Cách test:**

Bạn có thể dùng `curl` hoặc kết nối trực tiếp từ trình duyệt:

```bash
curl -N http://localhost:8080/api/memes/random-stream
```

Client sẽ giữ kết nối mở và mỗi 5 phút, một sự kiện tên là `random-meme` chứa dữ liệu của một meme ngẫu nhiên sẽ được gửi về.