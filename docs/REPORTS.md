# Article Report Feature Documentation

## Overview
The Article Report feature allows authenticated users to flag inappropriate or problematic articles for moderator review. This system includes predefined report categories, duplicate prevention, rate limiting, and a comprehensive admin dashboard for managing reports.

## Features

### Report Categories
Users can report articles for the following reasons:

| Category | Description |
|----------|-------------|
| `SPAM_MISLEADING` | Spam, clickbait, or misleading content |
| `INAPPROPRIATE_LANGUAGE` | Hate speech, harassment, or inappropriate language |
| `COPYRIGHT_INFRINGEMENT` | Unauthorized use of copyrighted material |
| `MISINFORMATION` | False information or misinformation |
| `VIOLENCE_HARMFUL` | Violent, dangerous, or harmful content |
| `OTHER` | Other issues (requires detailed explanation) |

### Report Status Workflow

```
PENDING → UNDER_REVIEW → RESOLVED (Action Taken)
                      ↘ DISMISSED (No Action Needed)
```

| Status | Description |
|--------|-------------|
| `PENDING` | Report submitted, awaiting review |
| `UNDER_REVIEW` | Moderator is currently reviewing |
| `RESOLVED` | Report validated, action taken |
| `DISMISSED` | Report reviewed and dismissed as invalid |

### Security Features

1. **Duplicate Prevention**: Users cannot report the same article more than once
2. **Rate Limiting**: Maximum 5 reports per user per hour
3. **Authentication Required**: Must be logged in to submit reports
4. **Admin Authorization**: Only ADMIN role can manage reports
5. **Input Validation**: All fields validated with length and format checks

---

## API Endpoints

### User Endpoints (Authenticated Users)

#### 1. Submit a Report

Report an inappropriate article.

- **Method:** `POST`
- **URL:** `http://localhost:8080/api/v1/posts/{postId}/report`
- **Authorization:** Required (Bearer Token)
- **Path Variable:** `postId` - ID of the post to report
- **Content-Type:** `application/json`

**Request Body:**
```json
{
  "category": "SPAM_MISLEADING",
  "description": "This post contains misleading information and spam links"
}
```

**Note:** The `description` field is optional for all categories except `OTHER`, where it is required.

**Success Response (201 Created):**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "postId": "post-123",
  "postTitle": "Example Article Title",
  "postSlug": "example-article-title",
  "category": "SPAM_MISLEADING",
  "description": "This post contains misleading information and spam links",
  "status": "PENDING",
  "createdAt": "2026-04-15T10:30:00",
  "updatedAt": "2026-04-15T10:30:00"
}
```

**Error Responses:**

- **400 Bad Request** - Invalid category or missing description for OTHER
- **404 Not Found** - Post or user not found
- **409 Conflict** - User already reported this post
- **429 Too Many Requests** - Rate limit exceeded (5 reports/hour)

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/posts/post-123/report \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "category": "SPAM_MISLEADING",
    "description": "This post contains spam content"
  }'
```

---

#### 2. View Your Report

Check the status of a report you submitted.

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/v1/posts/reports/{reportId}`
- **Authorization:** Required (Bearer Token)
- **Path Variable:** `reportId` - ID of the report

**Success Response (200 OK):**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "postId": "post-123",
  "postTitle": "Example Article Title",
  "postSlug": "example-article-title",
  "category": "SPAM_MISLEADING",
  "description": "This post contains misleading information",
  "status": "UNDER_REVIEW",
  "createdAt": "2026-04-15T10:30:00",
  "updatedAt": "2026-04-15T11:00:00"
}
```

**Error Responses:**
- **403 Forbidden** - Trying to view another user's report
- **404 Not Found** - Report not found

---

### Admin Endpoints (ADMIN Role Only)

#### 3. List All Reports

Get a paginated list of all reports with optional filtering.

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/v1/admin/reports`
- **Authorization:** Required (ADMIN role)
- **Query Parameters:**
  - `status` (optional) - Filter by status: `PENDING`, `UNDER_REVIEW`, `RESOLVED`, `DISMISSED`
  - `category` (optional) - Filter by category
  - `page` (default: 0) - Page number
  - `size` (default: 20) - Page size
  - `sort` (default: createdAt,desc) - Sort field and direction

**Example URLs:**
```
GET /api/v1/admin/reports
GET /api/v1/admin/reports?status=PENDING
GET /api/v1/admin/reports?category=SPAM_MISLEADING&status=PENDING
GET /api/v1/admin/reports?page=0&size=50&sort=createdAt,asc
```

**Success Response (200 OK):**
```json
{
  "content": [
    {
      "id": "report-001",
      "postId": "post-123",
      "postTitle": "Article Title",
      "postSlug": "article-title",
      "postAuthor": "author_username",
      "reporterId": "user-456",
      "reporterUsername": "reporter_user",
      "reporterEmail": "reporter@example.com",
      "category": "SPAM_MISLEADING",
      "description": "Spam content detected",
      "status": "PENDING",
      "adminNotes": null,
      "reviewedBy": null,
      "reviewedAt": null,
      "createdAt": "2026-04-15T10:30:00",
      "updatedAt": "2026-04-15T10:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    }
  },
  "totalElements": 150,
  "totalPages": 8,
  "last": false,
  "first": true,
  "numberOfElements": 20,
  "size": 20,
  "number": 0
}
```

---

#### 4. Get Pending Reports Queue

Get reports awaiting moderation (sorted by oldest first).

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/v1/admin/reports/pending`
- **Authorization:** Required (ADMIN role)
- **Query Parameters:** Standard pagination params

**Success Response (200 OK):** Same structure as "List All Reports"

---

#### 5. Get Report Details

Get full details of a specific report.

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/v1/admin/reports/{reportId}`
- **Authorization:** Required (ADMIN role)
- **Path Variable:** `reportId` - ID of the report

**Success Response (200 OK):**
```json
{
  "id": "report-001",
  "postId": "post-123",
  "postTitle": "Article Title",
  "postSlug": "article-title",
  "postAuthor": "author_username",
  "reporterId": "user-456",
  "reporterUsername": "reporter_user",
  "reporterEmail": "reporter@example.com",
  "category": "SPAM_MISLEADING",
  "description": "Detailed explanation of the issue",
  "status": "PENDING",
  "adminNotes": null,
  "reviewedBy": null,
  "reviewedAt": null,
  "createdAt": "2026-04-15T10:30:00",
  "updatedAt": "2026-04-15T10:30:00"
}
```

---

#### 6. Update Report Status

Change the status of a report and add moderator notes.

- **Method:** `PUT`
- **URL:** `http://localhost:8080/api/v1/admin/reports/{reportId}/status`
- **Authorization:** Required (ADMIN role)
- **Path Variable:** `reportId` - ID of the report
- **Content-Type:** `application/json`

**Request Body:**
```json
{
  "status": "RESOLVED",
  "adminNotes": "Verified spam content. Post has been removed and user warned."
}
```

**Valid Status Values:**
- `UNDER_REVIEW` - Mark as being reviewed
- `RESOLVED` - Mark as resolved with action taken
- `DISMISSED` - Mark as invalid/unfounded

**Success Response (200 OK):**
```json
{
  "id": "report-001",
  "postId": "post-123",
  "postTitle": "Article Title",
  "postSlug": "article-title",
  "postAuthor": "author_username",
  "reporterId": "user-456",
  "reporterUsername": "reporter_user",
  "reporterEmail": "reporter@example.com",
  "category": "SPAM_MISLEADING",
  "description": "Spam content detected",
  "status": "RESOLVED",
  "adminNotes": "Verified spam content. Post has been removed and user warned.",
  "reviewedBy": "admin_username",
  "reviewedAt": "2026-04-15T11:00:00",
  "createdAt": "2026-04-15T10:30:00",
  "updatedAt": "2026-04-15T11:00:00"
}
```

**cURL Example:**
```bash
curl -X PUT http://localhost:8080/api/v1/admin/reports/report-001/status \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "RESOLVED",
    "adminNotes": "Content verified as spam. Post removed."
  }'
```

---

#### 7. Delete Report

Permanently delete a report.

- **Method:** `DELETE`
- **URL:** `http://localhost:8080/api/v1/admin/reports/{reportId}`
- **Authorization:** Required (ADMIN role)
- **Path Variable:** `reportId` - ID of the report

**Success Response (204 No Content)**

---

#### 8. Get Report Statistics

Get statistics for the admin dashboard.

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/v1/admin/reports/statistics`
- **Authorization:** Required (ADMIN role)

**Success Response (200 OK):**
```json
{
  "totalReports": 150,
  "pendingReports": 23,
  "resolvedReports": 110,
  "dismissedReports": 17,
  "reportsToday": 8,
  "byCategory": {
    "SPAM_MISLEADING": 45,
    "INAPPROPRIATE_LANGUAGE": 30,
    "COPYRIGHT_INFRINGEMENT": 25,
    "MISINFORMATION": 20,
    "VIOLENCE_HARMFUL": 15,
    "OTHER": 15
  }
}
```

---

## Error Handling

### Common Error Responses

#### 400 Bad Request
```json
{
  "timestamp": "2026-04-15T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/posts/post-123/report"
}
```

#### 401 Unauthorized
```json
{
  "timestamp": "2026-04-15T10:30:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required",
  "path": "/api/v1/posts/post-123/report"
}
```

#### 403 Forbidden
```json
{
  "timestamp": "2026-04-15T10:30:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "You can only view your own reports",
  "path": "/api/v1/posts/reports/report-001"
}
```

#### 409 Conflict (Duplicate Report)
```json
{
  "timestamp": "2026-04-15T10:30:00.000+00:00",
  "status": 409,
  "error": "Conflict",
  "message": "You have already reported this post",
  "path": "/api/v1/posts/post-123/report"
}
```

#### 429 Too Many Requests (Rate Limit)
```json
{
  "timestamp": "2026-04-15T10:30:00.000+00:00",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Too many reports submitted. Please wait before submitting more reports. Maximum 5 reports per hour.",
  "path": "/api/v1/posts/post-123/report"
}
```

---

## Testing Guide

### Prerequisites
1. User must be authenticated (have a valid JWT token)
2. Admin endpoints require ADMIN role
3. Post must exist in the database

### Test Scenarios

#### 1. Submit Valid Report
```bash
# Login and get JWT token
curl -X POST http://localhost:8080/api/v1/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'

# Submit report
curl -X POST http://localhost:8080/api/v1/posts/POST_ID/report \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"category":"SPAM_MISLEADING","description":"Spam content"}'
```

#### 2. Test Duplicate Prevention
```bash
# Submit same report twice - second should fail with 409
curl -X POST http://localhost:8080/api/v1/posts/POST_ID/report \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"category":"SPAM_MISLEADING","description":"Spam content"}'
```

#### 3. Test Rate Limiting
```bash
# Submit 6 reports within 1 hour - 6th should fail with 429
for i in {1..6}; do
  curl -X POST http://localhost:8080/api/v1/posts/POST_ID_$i/report \
    -H "Authorization: Bearer YOUR_JWT_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"category\":\"SPAM_MISLEADING\",\"description\":\"Test $i\"}"
done
```

#### 4. Admin: Update Report Status
```bash
# Login as admin
curl -X POST http://localhost:8080/api/v1/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Update report status
curl -X PUT http://localhost:8080/api/v1/admin/reports/REPORT_ID/status \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"RESOLVED","adminNotes":"Verified and action taken"}'
```

#### 5. Admin: Get Statistics
```bash
curl -X GET http://localhost:8080/api/v1/admin/reports/statistics \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

---

## Database Schema

### Table: `article_reports`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | VARCHAR(36) | PRIMARY KEY | Unique report ID (UUID) |
| `post_id` | VARCHAR(36) | NOT NULL, FK | Reported post ID |
| `reporter_id` | VARCHAR(36) | NOT NULL, FK | User who submitted report |
| `category` | VARCHAR(50) | NOT NULL | Report category enum |
| `description` | TEXT | NULL | Detailed explanation |
| `status` | VARCHAR(20) | NOT NULL | Report status enum |
| `admin_notes` | TEXT | NULL | Moderator notes |
| `reviewed_by` | VARCHAR(36) | NULL, FK | Admin who reviewed |
| `reviewed_at` | DATETIME | NULL | When report was reviewed |
| `created_at` | DATETIME | NOT NULL | When report was created |
| `updated_at` | DATETIME | NULL | Last update timestamp |

### Indexes
- `idx_report_post` - Fast lookup by post
- `idx_report_user` - Fast lookup by reporter
- `idx_report_status` - Fast filtering by status
- `idx_report_category` - Fast filtering by category
- `idx_report_created` - Fast sorting by date

### Constraints
- `uk_user_post_report` - UNIQUE(reporter_id, post_id) prevents duplicates

---

## Integration Points

### Notification System
When a report is submitted:
- Post author receives notification: "Your post '[title]' has been reported for review"
- Notification type: `REPORT_SUBMITTED`
- Integrated with existing `NotificationService`

### Security Configuration
Endpoints added to `SecurityConfig.java`:
- `/api/v1/posts/{postId}/report` - Authenticated users
- `/api/v1/posts/reports/**` - Authenticated users
- `/api/v1/admin/reports/**` - ADMIN role only

---

## Best Practices

### For Users
1. **Provide Details**: Always include a clear description, especially for OTHER category
2. **Choose Correct Category**: Select the most appropriate category for the violation
3. **One Report Per Issue**: Don't submit multiple reports for the same issue
4. **Wait for Review**: Reports are typically reviewed within 24-48 hours

### For Administrators
1. **Review Promptly**: Process pending reports within 24 hours
2. **Add Notes**: Always document your decision in adminNotes
3. **Be Consistent**: Apply the same standards across all reports
4. **Monitor Statistics**: Use the statistics endpoint to identify trends

---

## Troubleshooting

### Issue: "You have already reported this post"
**Solution**: A user can only report a post once. Check your existing reports.

### Issue: "Too many reports submitted"
**Solution**: Rate limit is 5 reports per hour. Wait before submitting more.

### Issue: "Description is required when category is OTHER"
**Solution**: When selecting OTHER category, you must provide a detailed explanation.

### Issue: "Forbidden - You can only view your own reports"
**Solution**: Users can only view reports they submitted. Admins can view all reports.

---

## Future Enhancements

Potential improvements for future versions:

1. **Auto-Moderation**: Automatically hide posts with N+ reports
2. **Email Notifications**: Send emails to admins for high-priority reports
3. **Bulk Actions**: Resolve/dismiss multiple reports at once
4. **Report Comments**: Allow moderator discussion on reports
5. **Export Reports**: CSV/PDF export for compliance
6. **Webhook Integration**: Notify external moderation systems
7. **Report Analytics**: Charts and trends in admin dashboard
8. **User Warning System**: Automatic warnings for frequently reported authors

---

## API Reference Summary

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/posts/{postId}/report` | User | Submit a report |
| GET | `/api/v1/posts/reports/{reportId}` | User | View own report |
| GET | `/api/v1/admin/reports` | Admin | List all reports |
| GET | `/api/v1/admin/reports/pending` | Admin | Get pending queue |
| GET | `/api/v1/admin/reports/{reportId}` | Admin | Get report details |
| PUT | `/api/v1/admin/reports/{reportId}/status` | Admin | Update status |
| DELETE | `/api/v1/admin/reports/{reportId}` | Admin | Delete report |
| GET | `/api/v1/admin/reports/statistics` | Admin | Get statistics |

---

## Support

For issues or questions:
- Check Swagger UI: `http://localhost:8080/swagger-ui.html`
- Review server logs for detailed error messages
- Contact system administrator for access issues
