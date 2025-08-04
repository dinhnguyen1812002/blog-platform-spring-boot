# Hướng dẫn Test API Quản lý (Admin)

Các API này yêu cầu quyền `ROLE_ADMIN`.

## 1. Quản lý Category (CRUD)

-   **Lấy tất cả:** `GET /api/v1/categories`
-   **Tạo mới:** `POST /api/v1/categories` (Body: `{"name": "Tên Category"}`)
-   **Cập nhật:** `PUT /api/v1/categories/{id}` (Body: `{"name": "Tên Category Mới"}`)
-   **Xóa:** `DELETE /api/v1/categories/{id}`

## 2. Quản lý Tag (CRUD)

-   **Lấy tất cả:** `GET /api/v1/tags`
-   **Tạo mới:** `POST /api/v1/tags` (Body: `{"name": "Tên Tag"}`)
-   **Cập nhật:** `PUT /api/v1/tags/{id}` (Body: `{"name": "Tên Tag Mới"}`)
-   **Xóa:** `DELETE /api/v1/tags/{id}`
