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

---

# Analytics API Documentation

## Overview

The Analytics feature provides comprehensive administrative analytics for the blog platform, including dashboard summaries, monthly growth trends, top content, and report export capabilities.

**Base URL:** `/api/admin/analytics`

**Security:** All endpoints require `ROLE_ADMIN` authority.

---

## 1. Dashboard Summary

Returns summary statistics for the platform dashboard.

### Endpoint
```
GET /api/admin/analytics/summary
```

### Response
```json
{
  "success": true,
  "message": "Dashboard summary retrieved successfully",
  "data": {
    "totalViews": 15000,
    "activeUsers": 250,
    "newPosts": 45,
    "totalLikes": 3200
  }
}
```

### Fields
| Field | Description |
|-------|-------------|
| totalViews | Sum of all post views across the platform |
| activeUsers | Users who logged in within the last 30 days |
| newPosts | Posts created in the current month |
| totalLikes | Total likes across all posts |

---

## 2. Monthly Growth Analytics

Returns monthly growth statistics for a specified year.

### Endpoint
```
GET /api/admin/analytics/growth/monthly?year=2024
```

### Query Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| year | int | Yes | Year to retrieve analytics for |

### Response
```json
{
  "success": true,
  "message": "Monthly growth analytics retrieved successfully",
  "data": [
    {
      "year": 2024,
      "month": 1,
      "newUsers": 45,
      "newPosts": 120,
      "totalViews": 5000,
      "totalLikes": 360,
      "growthRate": 15.5
    }
  ]
}
```

---

## 3. New Users Per Month

Returns count of newly registered users grouped by month.

### Endpoint
```
GET /api/admin/analytics/users/monthly?year=2024
```

### Response
```json
{
  "success": true,
  "message": "New users per month retrieved successfully",
  "data": [
    {
      "year": 2024,
      "month": 1,
      "count": 45
    },
    {
      "year": 2024,
      "month": 2,
      "count": 52
    }
  ]
}
```

---

## 4. Post Growth Per Month

Returns count of new posts grouped by month.

### Endpoint
```
GET /api/admin/analytics/posts/monthly?year=2024
```

---

## 5. Most Viewed Posts

Returns top N posts ranked by view count.

### Endpoint
```
GET /api/admin/analytics/posts/top-viewed?limit=10
```

### Query Parameters
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| limit | int | 10 | Number of top posts to return |

### Response
```json
{
  "success": true,
  "message": "Most viewed posts retrieved successfully",
  "data": [
    {
      "postId": "uuid-here",
      "title": "Post Title",
      "authorName": "author_username",
      "viewCount": 1500,
      "likeCount": 45
    }
  ]
}
```

---

## 6. Most Liked Posts

Returns top N posts ranked by like count.

### Endpoint
```
GET /api/admin/analytics/posts/top-liked?limit=10
```

---

## 7. Most Popular Posts

Returns top N posts by combined popularity score (based on likes).

### Endpoint
```
GET /api/admin/analytics/posts/top-popular?limit=10
```

---

## 8. Top Authors

Returns top N authors ranked by total engagement (views + likes).

### Endpoint
```
GET /api/admin/analytics/authors/top?limit=10
```

### Response
```json
{
  "success": true,
  "message": "Top authors retrieved successfully",
  "data": [
    {
      "authorId": "uuid-here",
      "authorName": "author_username",
      "totalViews": 50000,
      "totalLikes": 2500,
      "postCount": 150
    }
  ]
}
```

---

## 9. Report Export

Exports analytics reports in PDF or Excel format.

### Endpoint
```
POST /api/admin/analytics/export
```

### Request Body
```json
{
  "reportType": "MONTH",
  "year": 2024,
  "month": 3,
  "quarter": null,
  "exportFormat": "PDF"
}
```

### Request Fields
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| reportType | string | Yes | `MONTH`, `QUARTER`, or `YEAR` |
| year | int | Yes | Year for the report |
| month | int | Conditionally | Month (1-12), required when reportType is MONTH |
| quarter | int | Conditionally | Quarter (1-4), required when reportType is QUARTER |
| exportFormat | string | Yes | `PDF` or `EXCEL` |

### Response
- **Content-Type:** `application/pdf` or `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- **Content-Disposition:** `attachment; filename="analytics_report_2024_03_20240115_143000.pdf"`

### Report Contents
The exported report includes multiple sheets/pages:
1. **Dashboard Summary** - Overall platform statistics
2. **Monthly Statistics** - Monthly trends for the selected period
3. **Top Posts** - Most viewed/liked posts
4. **Top Authors** - Most engaged authors

---

## Error Handling

All endpoints return standardized error responses:

```json
{
  "success": false,
  "message": "Error message here",
  "data": null
}
```

**Common Error Codes:**
- 403 Forbidden - User lacks ADMIN role
- 400 Bad Request - Invalid request parameters
- 500 Internal Server Error - Server-side error