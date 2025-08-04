# Hướng dẫn Test API Lưu bài viết (Bookmarks)

## 1. Lưu một bài viết

-   **Method:** `POST`
-   **URL:** `http://localhost:8080/api/v1/saved/save`
-   **Authorization:** Bắt buộc.
-   **Body:** `raw` - `JSON`

```json
{
  "postId": "uuid-cua-bai-viet"
}
```

## 2. Lấy danh sách bài viết đã lưu

-   **Method:** `GET`
-   **URL:** `http://localhost:8080/api/v1/saved/saved`
-   **Authorization:** Bắt buộc.
