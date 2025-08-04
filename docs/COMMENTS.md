# Hướng dẫn Test API Bình luận (Comments)

## 1. Thêm bình luận vào bài viết

-   **Method:** `POST`
-   **URL:** `http://localhost:8080/api/v1/comments/posts/{postId}`
-   **Authorization:** Bắt buộc.
-   **Path Variable:** `postId` - ID của bài viết.
-   **Body:** `raw` - `JSON`

```json
{
  "content": "Nội dung bình luận.",
  "parentId": null 
}
```
(Để `parentId` là `null` nếu là bình luận gốc, hoặc điền ID của bình luận cha nếu là trả lời).

## 2. Lấy bình luận của bài viết

-   **Method:** `GET`
-   **URL:** `http://localhost:8080/api/v1/comments/posts/{postId}`
-   **Authorization:** Không bắt buộc.
-   **Path Variable:** `postId` - ID của bài viết.
-   **Query Params:**
    -   `page`: Số trang (mặc định: `0`).
    -   `size`: Kích thước trang (mặc định: `10`).
    -   `sort`: Thuộc tính để sắp xếp (mặc định: `createdAt,desc`).

## 3. Lấy các câu trả lời cho một bình luận

-   **Method:** `GET`
-   **URL:** `http://localhost:8080/api/v1/comments/{commentId}/replies`
-   **Authorization:** Không bắt buộc.
-   **Path Variable:** `commentId` - ID của bình luận.
-   **Query Params:**
    -   `page`: Số trang (mặc định: `0`).
    -   `size`: Kích thước trang (mặc định: `5`).
    -   `sort`: Thuộc tính để sắp xếp (mặc định: `createdAt,desc`).