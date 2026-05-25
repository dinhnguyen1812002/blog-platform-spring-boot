
# Quản lý Series

Tính năng này cho phép người dùng tạo, quản lý và sắp xếp các bài viết trong một series.

## Các API

- [Tạo mới series](#tạo-mới-series)
- [Cập nhật thông tin series](#cập-nhật-thông-tin-series)
- [Lấy chi tiết series theo ID](#lấy-chi-tiết-series-theo-id)
- [Lấy chi tiết series theo slug](#lấy-chi-tiết-series-theo-slug)
- [Lấy danh sách tất cả series](#lấy-danh-sách-tất-cả-series)
- [Lấy danh sách series của một tác giả](#lấy-danh-sách-series-của-một-tác-giả)
- [Tìm kiếm series](#tìm-kiếm-series)
- [Lấy các series phổ biến nhất](#lấy-các-series-phổ-biến-nhất)
- [Thêm bài viết vào series](#thêm-bài-viết-vào-series)
- [Xóa bài viết khỏi series](#xóa-bài-viết-khỏi-series)
- [Sắp xếp lại thứ tự bài viết trong series](#sắp-xếp-lại-thứ-tự-bài-viết-trong-series)
- [Xóa series](#xóa-series)

---

### Tạo mới series

- **Endpoint:** `POST /api/v1/series`
- **Mô tả:** Tạo một series mới.
- **Yêu cầu:**
  - Header: `Authorization: Bearer <token>`
  - Body:
    ```json
    {
      "title": "Tiêu đề series",
      "slug": "slug-series",
      "description": "Mô tả series",
      "thumbnail": "https://example.com/image.jpg",
      "isActive": true,
      "isCompleted": false
    }
    ```
- **Validation:**
  - `title`: Bắt buộc, độ dài 5-200 ký tự
  - `slug`: Bắt buộc, độ dài 5-250 ký tự, phải là duy nhất
  - `description`: Tối thiểu 10 ký tự
  - `thumbnail`: Tùy chọn (URL hình ảnh)
  - `isActive`: Tùy chọn, mặc định là `true`
  - `isCompleted`: Tùy chọn, mặc định là `false`
- **Phản hồi:**
  ```json
  {
    "success": true,
    "message": "Series created successfully",
    "data": {
      "id": "string",
      "title": "string",
      "slug": "string",
      "description": "string",
      "thumbnail": "string",
      "userId": "string",
      "username": "string",
      "userAvatar": "string",
      "isActive": true,
      "isCompleted": false,
      "totalPosts": 0,
      "viewCount": 0,
      "createdAt": "2025-10-10T10:00:00.000Z",
      "updatedAt": "2025-10-10T10:00:00.000Z",
      "posts": []
    }
  }
  ```

---

### Cập nhật thông tin series

- **Endpoint:** `PUT /api/v1/series/{seriesId}`
- **Mô tả:** Cập nhật thông tin của một series.
- **Yêu cầu:**
  - Header: `Authorization: Bearer <token>`
  - Path variable: `seriesId`
  - Body:
    ```json
    {
      "title": "Tiêu đề series mới",
      "description": "Mô tả series mới",
      "thumbnail": "https://example.com/new-image.jpg",
      "isActive": true,
      "isCompleted": false
    }
    ```
- **Validation:**
  - `title`: Tùy chọn, độ dài 5-200 ký tự
  - `description`: Tùy chọn, tối thiểu 10 ký tự
  - `thumbnail`: Tùy chọn (URL hình ảnh)
  - `isActive`: Tùy chọn
  - `isCompleted`: Tùy chọn
- **Phản hồi:**
  ```json
  {
    "success": true,
    "message": "Series updated successfully",
    "data": {
      "id": "string",
      "title": "string",
      "slug": "string",
      "description": "string",
      "thumbnail": "string",
      "userId": "string",
      "username": "string",
      "userAvatar": "string",
      "isActive": true,
      "isCompleted": false,
      "totalPosts": 0,
      "viewCount": 0,
      "createdAt": "2025-10-10T10:00:00.000Z",
      "updatedAt": "2025-10-10T10:00:00.000Z",
      "posts": []
    }
  }
  ```

---

### Lấy chi tiết series theo ID

- **Endpoint:** `GET /api/v1/series/{seriesId}`
- **Mô tả:** Lấy thông tin chi tiết của một series bằng ID.
- **Yêu cầu:**
  - Path variable: `seriesId`
- **Phản hồi:**
  ```json
  {
    "success": true,
    "message": "Series retrieved successfully",
    "data": {
      "id": "string",
      "title": "string",
      "slug": "string",
      "description": "string",
      "thumbnail": "string",
      "userId": "string",
      "username": "string",
      "userAvatar": "string",
      "isActive": true,
      "isCompleted": false,
      "totalPosts": 5,
      "viewCount": 100,
      "createdAt": "2025-10-10T10:00:00.000Z",
      "updatedAt": "2025-10-10T10:00:00.000Z",
      "posts": [
        {
          "postId": "string",
          "title": "string",
          "slug": "string",
          "excerpt": "string",
          "thumbnail": "string",
          "orderIndex": 1,
          "addedAt": "2025-10-10T10:00:00.000Z",
          "publicDate": "2025-10-10T10:00:00.000Z"
        }
      ]
    }
  }
  ```

---

### Lấy chi tiết series theo slug

- **Endpoint:** `GET /api/v1/series/slug/{slug}`
- **Mô tả:** Lấy thông tin chi tiết của một series bằng slug.
- **Yêu cầu:**
  - Path variable: `slug`
- **Phản hồi:** (Tương tự như lấy theo ID)

---

### Lấy danh sách tất cả series

- **Endpoint:** `GET /api/v1/series`
- **Mô tả:** Lấy danh sách tất cả các series với phân trang.
- **Yêu cầu:**
  - Query params: `page` (mặc định: 0), `size` (mặc định: 10), `sortBy` (mặc định: createdAt), `sortDirection` (mặc định: DESC)
- **Phản hồi:**
  ```json
  {
    "success": true,
    "message": "Series list retrieved successfully",
    "data": {
      "content": [
        {
          "id": "string",
          "title": "string",
          "slug": "string",
          "description": "string",
          "thumbnail": "string",
          "username": "string",
          "userAvatar": "string",
          "isActive": true,
          "isCompleted": false,
          "totalPosts": 5,
          "viewCount": 100,
          "createdAt": "2025-10-10T10:00:00.000Z",
          "updatedAt": "2025-10-10T10:00:00.000Z"
        }
      ],
      "pageable": {
        "pageNumber": 0,
        "pageSize": 10,
        "sort": {
          "sorted": true,
          "unsorted": false,
          "empty": false
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
      },
      "last": true,
      "totalPages": 1,
      "totalElements": 1,
      "size": 10,
      "number": 0,
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      },
      "first": true,
      "numberOfElements": 1,
      "empty": false
    }
  }
  ```

---

### Lấy danh sách series của một tác giả

- **Endpoint:** `GET /api/v1/series/user/{userId}`
- **Mô tả:** Lấy danh sách các series của một tác giả cụ thể.
- **Yêu cầu:**
  - Path variable: `userId`
  - Query params: `page`, `size`, `sortBy`, `sortDirection`
- **Phản hồi:** (Tương tự như lấy tất cả series)

---

### Tìm kiếm series

- **Endpoint:** `POST /api/v1/series/search`
- **Mô tả:** Tìm kiếm series theo nhiều tiêu chí.
- **Yêu cầu:**
  - Body:
    ```json
    {
      "keyword": "từ khóa",
      "authorId": "string",
      "page": 0,
      "size": 10,
      "sortBy": "createdAt",
      "sortDirection": "DESC"
    }
    ```
- **Phản hồi:** (Tương tự như lấy tất cả series)

---

### Lấy các series phổ biến nhất

- **Endpoint:** `GET /api/v1/series/popular`
- **Mô tả:** Lấy danh sách các series được xem nhiều nhất.
- **Yêu cầu:**
  - Query params: `page`, `size`
- **Phản hồi:** (Tương tự như lấy tất cả series)

---

### Thêm bài viết vào series

- **Endpoint:** `POST /api/v1/series/{seriesId}/posts`
- **Mô tả:** Thêm một bài viết đã có vào series.
- **Yêu cầu:**
  - Header: `Authorization: Bearer <token>`
  - Path variable: `seriesId`
  - Body:
    ```json
    {
      "postId": "string",
      "orderIndex": 1
    }
    ```
- **Validation:**
  - `postId`: Bắt buộc
  - `orderIndex`: Tùy chọn, nếu null sẽ thêm vào cuối series
- **Phản hồi:** (Tương tự như cập nhật series)

---

### Xóa bài viết khỏi series

- **Endpoint:** `DELETE /api/v1/series/{seriesId}/posts/{postId}`
- **Mô tả:** Xóa một bài viết khỏi series.
- **Yêu cầu:**
  - Header: `Authorization: Bearer <token>`
  - Path variables: `seriesId`, `postId`
- **Phản hồi:** (Tương tự như cập nhật series)

---

### Sắp xếp lại thứ tự bài viết trong series

- **Endpoint:** `PUT /api/v1/series/{seriesId}/posts/reorder`
- **Mô tả:** Thay đổi thứ tự của một bài viết cụ thể trong series.
- **Yêu cầu:**
  - Header: `Authorization: Bearer <token>`
  - Path variable: `seriesId`
  - Body:
    ```json
    {
      "postId": "string",
      "newOrderIndex": 2
    }
    ```
- **Validation:**
  - `postId`: Bắt buộc
  - `newOrderIndex`: Bắt buộc, vị trí mới của bài viết (1-based)
- **Phản hồi:** (Tương tự như cập nhật series)

---

### Xóa series

- **Endpoint:** `DELETE /api/v1/series/{seriesId}`
- **Mô tả:** Xóa một series.
- **Yêu cầu:**
  - Header: `Authorization: Bearer <token>`
  - Path variable: `seriesId`
- **Phản hồi:**
  ```json
  {
    "success": true,
    "message": "Series deleted successfully",
    "data": null
  }
  ```
