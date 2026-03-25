# Hướng dẫn API Newsletter

Tài liệu này mô tả chi tiết các API để quản lý việc đăng ký nhận tin (Newsletter).

---

## 1. Luồng hoạt động

1.  **Người dùng đăng ký:** Người dùng cung cấp email để đăng ký.
2.  **Gửi email xác nhận:** Hệ thống gửi một email chứa liên kết xác nhận đến địa chỉ email đã đăng ký.
3.  **Người dùng xác nhận:** Người dùng nhấp vào liên kết trong email để xác thực địa chỉ email và kích hoạt việc nhận tin.
4.  **Hủy đăng ký:** Người dùng có thể hủy đăng ký bất cứ lúc nào thông qua API hoặc liên kết trong email newsletter.
5.  **Campaign:** Admin có thể tạo và gửi newsletter campaign đến tất cả subscribers đã kích hoạt.

---

## 2. Base URL

```
http://localhost:8080/api/v1/newsletter
```

---

## 3. API cho người dùng (Public)

Các API này không yêu cầu xác thực.

### 3.1. Đăng ký nhận tin

**Endpoint:** `POST /subscribe`

Đăng ký nhận bản tin. Một email xác nhận sẽ được gửi đi sau khi gọi API này thành công.

**Request Body:**
```json
{
  "email": "subscriber@example.com",
  "firstName": "Nguyen",
  "lastName": "Van A",
  "sourceUrl": "https://blogplatform.com",
  "gdprConsent": true
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| email | string | Yes | Địa chỉ email đăng ký |
| firstName | string | No | Tên người đăng ký |
| lastName | string | No | Họ người đăng ký |
| sourceUrl | string | No | URL nguồn đăng ký |
| gdprConsent | boolean | No | Đồng ý với điều khoản GDPR |

**Response Success (200 OK):**
```json
{
  "success": true,
  "message": "Please check your email to confirm your subscription.",
  "requiresConfirmation": true
}
```

**Response - Already Subscribed:**
```json
{
  "success": false,
  "message": "You are already subscribed to our newsletter!",
  "requiresConfirmation": false
}
```

**Response - Resend Confirmation:**
```json
{
  "success": true,
  "message": "Confirmation email has been resent. Please check your inbox.",
  "requiresConfirmation": true
}
```

---

### 3.2. Xác nhận đăng ký

**Endpoint:** `GET /confirm/{token}`

Xác nhận địa chỉ email bằng token được gửi qua email.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| token | string | Token xác nhận từ email |

**Example:**
```
GET http://localhost:8080/api/v1/newsletter/confirm/abc123-def456
```

**Response Success (200 OK):**
```json
{
  "message": "Your subscription has been confirmed successfully!"
}
```

**Response Error:**
```json
{
  "message": "Invalid or expired confirmation token"
}
```

---

### 3.3. Hủy đăng ký bằng token (One-click)

**Endpoint:** `GET /unsubscribe/{token}`

Hủy đăng ký bằng token (thường được sử dụng trong liên kết email).

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| token | string | Token hủy đăng ký từ email |

**Example:**
```
GET http://localhost:8080/api/v1/newsletter/unsubscribe/unsub-token-123
```

**Response Success (200 OK):**
```json
{
  "message": "You have been successfully unsubscribed."
}
```

---

### 3.4. Hủy đăng ký bằng email (API)

**Endpoint:** `POST /unsubscribe`

Hủy đăng ký qua API bằng địa chỉ email.

**Request Body:**
```json
{
  "email": "subscriber@example.com"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| email | string | Yes | Email đã đăng ký |

**Response Success (200 OK):**
```json
{
  "message": "You have been successfully unsubscribed."
}
```

**Response Error (Email not found):**
```json
{
  "message": "Email not found in our subscription list"
}
```

---

## 4. API cho Quản trị viên (Admin/Moderator)

Các API dưới đây yêu cầu quyền `ROLE_ADMIN` hoặc `ROLE_MODERATOR` và Bearer Token.

### 4.1. Lấy danh sách tất cả người đăng ký

**Endpoint:** `GET /subscribers`

Lấy danh sách tất cả người đã đăng ký, được phân trang và hỗ trợ tìm kiếm/lọc.

**Headers:**
```
Authorization: Bearer <admin_token>
```

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | integer | 0 | Số trang (bắt đầu từ 0) |
| size | integer | 10 | Số lượng mỗi trang |
| search | string | - | Tìm kiếm theo email |
| status | string | - | Lọc theo trạng thái: `PENDING`, `ACTIVE`, `UNSUBSCRIBED`, `BOUNCED`, `COMPLAINED`, `SUSPENDED` |

**Example:**
```
GET http://localhost:8080/api/v1/newsletter/subscribers?page=0&size=20&search=example&status=ACTIVE
```

**Response Success (200 OK):**
```json
{
  "content": [
    {
      "id": "uuid-123",
      "email": "subscriber@example.com",
      "firstName": "Nguyen",
      "lastName": "Van A",
      "status": "ACTIVE",
      "createdAt": "2024-01-15T10:30:00",
      "confirmedAt": "2024-01-15T10:35:00",
      "unsubscribedAt": null,
      "lastSentAt": "2024-01-20T08:00:00",
      "tags": null
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 125,
  "totalPages": 7
}
```

---

### 4.2. Lấy tổng số người đăng ký đã kích hoạt

**Endpoint:** `GET /subscribers/count`

Trả về tổng số lượng người đăng ký đang ở trạng thái `ACTIVE`.

**Headers:**
```
Authorization: Bearer <admin_token>
```

**Response Success (200 OK):**
```
125
```

---

### 4.3. Import hàng loạt người đăng ký

**Endpoint:** `POST /import`

Import nhiều người đăng ký cùng lúc từ danh sách.

**Headers:**
```
Authorization: Bearer <admin_token>
```

**Request Body:**
```json
{
  "subscribers": [
    {
      "email": "user1@example.com",
      "firstName": "User",
      "lastName": "One",
      "tags": "vip,early-adopter",
      "gdprConsent": true
    },
    {
      "email": "user2@example.com",
      "firstName": "User",
      "lastName": "Two",
      "tags": "regular",
      "gdprConsent": true
    }
  ],
  "requireConfirmation": false,
  "sendWelcomeEmail": true
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| subscribers | array | Yes | Danh sách người đăng ký |
| requireConfirmation | boolean | No | Yêu cầu xác nhận email (default: false) |
| sendWelcomeEmail | boolean | No | Gửi email chào mừng (default: true) |

**SubscriberImportRow:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| email | string | Yes | Email người đăng ký |
| firstName | string | No | Tên |
| lastName | string | No | Họ |
| tags | string | No | Tags phân loại |
| gdprConsent | boolean | No | Đồng ý GDPR |

**Response Success (200 OK):**
```json
{
  "imported": 2,
  "skipped": 1,
  "failed": 0,
  "errors": []
}
```

---

## 5. Campaign API (Admin/Moderator)

### 5.1. Tạo chiến dịch newsletter

**Endpoint:** `POST /campaigns`

Tạo một chiến dịch newsletter mới. Nếu có `scheduledAt`, chiến dịch sẽ được lên lịch gửi.

**Headers:**
```
Authorization: Bearer <admin_token>
```

**Request Body:**
```json
{
  "name": "Monthly Newsletter January 2024",
  "subject": "🎉 Check out our latest updates!",
  "htmlContent": "<html><body><h1>Hello {{firstName}}!</h1><p>...</p><a href='{{unsubscribeUrl}}'>Unsubscribe</a></body></html>",
  "textContent": "Hello! Check out our latest updates...",
  "fromName": "Blog Platform Team",
  "fromEmail": "newsletter@blogplatform.com",
  "replyTo": "support@blogplatform.com",
  "scheduledAt": "2024-01-25T09:00:00",
  "targetSegment": "all",
  "targetTags": "vip,regular",
  "batchSize": 100,
  "sendIntervalSeconds": 1,
  "utmSource": "newsletter",
  "utmMedium": "email",
  "utmCampaign": "january_2024"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | string | Yes | Tên chiến dịch |
| subject | string | Yes | Tiêu đề email |
| htmlContent | string | Yes | Nội dung HTML (hỗ trợ `{{firstName}}`, `{{email}}`, `{{unsubscribeUrl}}`) |
| textContent | string | No | Nội dung text thuần |
| fromName | string | No | Tên người gửi |
| fromEmail | string | No | Email người gửi |
| replyTo | string | No | Email reply-to |
| scheduledAt | datetime | No | Thời gian lên lịch gửi (ISO 8601) |
| targetSegment | string | No | Phân khúc mục tiêu |
| targetTags | string | No | Tags để lọc người nhận |
| batchSize | integer | No | Số email mỗi batch (default: 100) |
| sendIntervalSeconds | integer | No | Thời gian nghỉ giữa các email (giây) |
| utmSource | string | No | UTM source tracking |
| utmMedium | string | No | UTM medium tracking |
| utmCampaign | string | No | UTM campaign tracking |

**Response Success (200 OK):**
```json
{
  "id": "campaign-uuid-123",
  "name": "Monthly Newsletter January 2024",
  "subject": "🎉 Check out our latest updates!",
  "status": "SCHEDULED",
  "scheduledAt": "2024-01-25T09:00:00",
  "sentAt": null,
  "recipientCount": null,
  "sentCount": 0,
  "openedCount": 0,
  "clickedCount": 0,
  "bouncedCount": 0,
  "unsubscribedCount": 0,
  "createdAt": "2024-01-20T10:00:00"
}
```

**Status Values:**
- `DRAFT` - Đang nháp
- `SCHEDULED` - Đã lên lịch
- `SENDING` - Đang gửi
- `SENT` - Đã gửi xong
- `PAUSED` - Tạm dừng
- `CANCELLED` - Đã hủy
- `FAILED` - Thất bại

---

### 5.2. Gửi chiến dịch ngay lập tức

**Endpoint:** `POST /campaigns/{id}/send`

Gửi chiến dịch newsletter ngay lập tức (chỉ cho chiến dịch ở trạng thái `DRAFT` hoặc `SCHEDULED`).

**Headers:**
```
Authorization: Bearer <admin_token>
```

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| id | string | ID của chiến dịch |

**Example:**
```
POST http://localhost:8080/api/v1/newsletter/campaigns/campaign-uuid-123/send
```

**Response Success (200 OK):**
```json
{
  "message": "Campaign is being sent"
}
```

---

### 5.3. Lấy danh sách tất cả chiến dịch

**Endpoint:** `GET /campaigns`

Lấy danh sách tất cả chiến dịch newsletter với phân trang.

**Headers:**
```
Authorization: Bearer <admin_token>
```

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | integer | 0 | Số trang |
| size | integer | 10 | Số lượng mỗi trang |

**Example:**
```
GET http://localhost:8080/api/v1/newsletter/campaigns?page=0&size=10
```

**Response Success (200 OK):**
```json
{
  "content": [
    {
      "id": "campaign-uuid-123",
      "name": "Monthly Newsletter January 2024",
      "subject": "🎉 Check out our latest updates!",
      "status": "SENT",
      "scheduledAt": "2024-01-25T09:00:00",
      "sentAt": "2024-01-25T09:05:30",
      "recipientCount": 150,
      "sentCount": 150,
      "openedCount": 89,
      "clickedCount": 45,
      "bouncedCount": 2,
      "unsubscribedCount": 1,
      "createdAt": "2024-01-20T10:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 25,
  "totalPages": 3
}
```

---

## 6. Trạng thái người đăng ký (ENewsletterStatus)

| Status | Mô tả |
|--------|-------|
| `PENDING` | Đang chờ xác nhận email |
| `ACTIVE` | Đã kích hoạt, đang nhận newsletter |
| `UNSUBSCRIBED` | Đã hủy đăng ký |
| `BOUNCED` | Email bị bounce |
| `COMPLAINED` | Đã khiếu nại spam |
| `SUSPENDED` | Bị tạm ngưng |

---

## 7. Trạng thái chiến dịch (ECampaignStatus)

| Status | Mô tả |
|--------|-------|
| `DRAFT` | Bản nháp, chưa sẵn sàng gửi |
| `SCHEDULED` | Đã lên lịch gửi |
| `SENDING` | Đang trong quá trình gửi |
| `SENT` | Đã gửi hoàn tất |
| `PAUSED` | Tạm dừng gửi |
| `CANCELLED` | Đã hủy |
| `FAILED` | Gửi thất bại |

---

## 8. Biến mẫu trong HTML Campaign

Khi tạo nội dung HTML cho chiến dịch, có thể sử dụng các biến sau:

| Biến | Mô tả | Ví dụ |
|------|-------|-------|
| `{{firstName}}` | Tên người đăng ký | "Nguyen" hoặc "Subscriber" |
| `{{email}}` | Email người đăng ký | "user@example.com" |
| `{{unsubscribeUrl}}` | URL hủy đăng ký | `http://localhost:8080/api/v1/newsletter/unsubscribe/abc123` |

---

## 9. Scheduled Tasks

Hệ thống tự động thực hiện các tác vụ sau:

| Task | Cron | Mô tả |
|------|------|-------|
| Process Scheduled Campaigns | `0 */5 * * * *` | Kiểm tra và gửi chiến dịch đã đến lịch (mỗi 5 phút) |
| Cleanup Expired Subscriptions | `0 0 */6 * * *` | Xóa đăng ký PENDING hết hạn (mỗi 6 giờ) |

---

## 10. Ví dụ sử dụng với cURL

### Đăng ký nhận tin:
```bash
curl -X POST http://localhost:8080/api/v1/newsletter/subscribe \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "firstName": "Test",
    "lastName": "User",
    "gdprConsent": true
  }'
```

### Xác nhận đăng ký:
```bash
curl -X GET "http://localhost:8080/api/v1/newsletter/confirm/abc123-token"
```

### Hủy đăng ký:
```bash
curl -X POST http://localhost:8080/api/v1/newsletter/unsubscribe \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com"}'
```

### Lấy danh sách subscribers (Admin):
```bash
curl -X GET "http://localhost:8080/api/v1/newsletter/subscribers?page=0&size=10&status=ACTIVE" \
  -H "Authorization: Bearer <admin_token>"
```

### Tạo campaign (Admin):
```bash
curl -X POST http://localhost:8080/api/v1/newsletter/campaigns \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Campaign",
    "subject": "Test Subject",
    "htmlContent": "<html><body><h1>Hello {{firstName}}!</h1></body></html>",
    "fromName": "Admin",
    "scheduledAt": "2024-12-31T23:59:59"
  }'
```

### Import subscribers (Admin):
```bash
curl -X POST http://localhost:8080/api/v1/newsletter/import \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "subscribers": [
      {"email": "user1@example.com", "firstName": "User1", "gdprConsent": true},
      {"email": "user2@example.com", "firstName": "User2", "gdprConsent": true}
    ],
    "requireConfirmation": false,
    "sendWelcomeEmail": true
  }'
```

### Gửi campaign ngay (Admin):
```bash
curl -X POST "http://localhost:8080/api/v1/newsletter/campaigns/{campaign-id}/send" \
  -H "Authorization: Bearer <admin_token>"
```

---

## 11. Configuration

Các thuộc tính cấu hình trong `application.properties`:

```properties
# Base URL cho confirmation/unsubscribe links
newsletter.confirmation.base-url=http://localhost:8080

# Email gửi mặc định
newsletter.from-email=noreply@blogplatform.com

# Batch size mặc định
newsletter.batch.size=100
```
