# Hướng dẫn Test API Upload File

-   **Method:** `POST`
-   **URL:** `http://localhost:8080/api/v1/upload`
-   **Authorization:** Bắt buộc.
-   **Body:** `form-data`
    -   **Key:** `file`
    -   **Value:** Chọn một file hình ảnh từ máy của bạn.

- **Phản hồi thành công (200 OK):**

    ```json
    {
        "statusCode": 200,
        "message": "http://localhost:8080/uploads/thumbnail/638862458364170000_40982a8167f0a53dedce3731178f2ef5.jpg"
    }
    ```