# API Documentation: Create Post

This document provides details about the API endpoint for creating a new post.

## Endpoint

`POST /api/v1/author/write`

## Description

This endpoint allows an authenticated author to create a new blog post.

## Workflow

1.  **(Optional) Upload Thumbnail:** If the post has a thumbnail image, the client must first upload the image file to the `/api/v1/upload` endpoint. This will return a URL for the uploaded image.
2.  **Create Post:** The client then calls this `/api/v1/author/write` endpoint, including the thumbnail URL (if applicable) in the request body.

## Request

### Headers

| Header          | Value                | Description                                |
| --------------- | -------------------- | ------------------------------------------ |
| `Authorization` | `Bearer <JWT_TOKEN>` | The JWT token for authenticating the user. |
| `Content-Type`  | `application/json`   | The content type of the request body.      |

### Body

The request body should be a JSON object with the following properties:

```json
{
  "title": "Your Post Title",
  "excerpt": "A short summary of your post content (min 5 chars)",
  "content": "The full content of your post (min 10 chars).",
  "thumbnail": "URL_of_the_thumbnail_image",
  "categories": [1, 2],
  "tags": ["550e8400-e29b-41d4-a716-446655440000"],
  "featured": false,
  "visibility": "PUBLISHED",
  "scheduledPublishAt": "2025-09-01T10:00:00",
  "publishedAt": "2025-08-01T10:00:00"
}
```

### Body Parameters

| Parameter            | Type              | Required | Description                                                                                                                         |
| -------------------- | ----------------- | -------- | ----------------------------------------------------------------------------------------------------------------------------------- |
| `title`              | `String`          | **Yes**  | The title of the post. **Min: 5 chars, Max: 200 chars.**                                                                            |
| `excerpt`            | `String`          | **Yes**  | A short summary/excerpt of the post. **Min: 5 chars.**                                                                              |
| `content`            | `String`          | **Yes**  | The main content of the post, can be in HTML or Markdown format. **Min: 10 chars.**                                                 |
| `thumbnail`          | `String`          | No       | The URL of the post's thumbnail image. This should be obtained from the `/api/v1/upload` endpoint.                                  |
| `categories`         | `Set<Long>`       | **Yes**  | A set of **category IDs** to associate with the post. At least one category is required.                                            |
| `tags`               | `Set<UUID>`       | No       | A set of **tag IDs** to associate with the post.                                                                                    |
| `featured`           | `Boolean`         | No       | Whether the post should be marked as "featured". Defaults to `false`.                                                               |
| `visibility`         | `PublishStatus`   | No       | The visibility status of the post. Values: `PUBLISHED`, `SCHEDULED`, `PRIVATE`, `DRAFT`. Defaults to `DRAFT`.                       |
| `scheduledPublishAt` | `LocalDateTime`   | No       | The date and time when the post should be published if visibility is `SCHEDULED`, in ISO-8601 format. Must be in the **future**.      |
| `publishedAt`        | `LocalDateTime`   | No       | Custom publish date for the post. Used when visibility is `PUBLISHED`. Defaults to current time if not provided.                    |

### Visibility Values

| Value       | Description                                                                                  |
| ----------- | -------------------------------------------------------------------------------------------- |
| `DRAFT`     | Post is saved as draft, not visible to public.                                               |
| `PUBLISHED` | Post is published immediately (or at `publishedAt` if provided).                             |
| `SCHEDULED` | Post will be published automatically at `scheduledPublishAt`. Must provide future date/time. |
| `PRIVATE`   | Post is private, only visible to the author.                                                 |

## Responses

### Success Response

- **Status Code:** `201 Created`
- **Content:** A `PostResponse` object containing the created post details.

```json
{
  "id": "post-uuid",
  "title": "Your Post Title",
  "slug": "your-post-title",
  "excerpt": "A short summary...",
  "content": "The full content...",
  "thumbnail": "http://example.com/uploads/thumbnail.jpg",
  "author": {
    "id": "author-uuid",
    "username": "authorname",
    "avatar": "http://example.com/avatar.jpg"
  },
  "categories": [
    { "id": 1, "name": "Technology", "slug": "technology" }
  ],
  "tags": [
    { "id": "uuid", "name": "java", "slug": "java" }
  ],
  "featured": false,
  "visibility": "PUBLISHED",
  "publishedAt": "2025-08-01T10:00:00",
  "scheduledPublishAt": null,
  "createdAt": "2025-08-01T09:00:00",
  "updatedAt": "2025-08-01T09:00:00",
  "viewCount": 0,
  "likesCount": 0,
  "commentsCount": 0,
  "averageRating": 0.0,
  "isLikedByCurrentUser": false,
  "isBookmarkedByCurrentUser": false
}
```

### Error Responses

- **Status Code:** `400 Bad Request`
  - Missing required fields (`title`, `excerpt`, `content`, `categories`)
  - Field validation errors (length constraints)
  - Title already exists
  - Invalid `scheduledPublishAt` (must be in the future for SCHEDULED visibility)

  ```json
  {
    "message": "Title is required"
  }
  ```

- **Status Code:** `401 Unauthorized`
  - User is not authenticated or JWT token is invalid/expired.

  ```json
  {
    "message": "User not authenticated or invalid authentication type"
  }
  ```

- **Status Code:** `403 Forbidden`
  - User doesn't have author permissions.

- **Status Code:** `404 Not Found`
  - Category ID or Tag ID not found in the system.

  ```json
  {
    "message": "Category not found"
  }
  ```

- **Status Code:** `500 Internal Server Error`
  - Unexpected server error.

  ```json
  {
    "message": "Error creating post: <error_details>"
  }
  ```

## Example cURL Requests

### Create a Published Post

```bash
curl -X POST http://localhost:8080/api/v1/author/write \
-H "Authorization: Bearer <your_jwt_token>" \
-H "Content-Type: application/json" \
-d '{
      "title": "My First Post",
      "excerpt": "This is a short summary of my first post.",
      "content": "<h1>Hello World!</h1><p>This is my first post with detailed content.</p>",
      "thumbnail": "http://example.com/uploads/my-thumbnail.jpg",
      "categories": [1, 2],
      "tags": ["550e8400-e29b-41d4-a716-446655440000"],
      "visibility": "PUBLISHED",
      "featured": false
    }'
```

### Create a Scheduled Post

```bash
curl -X POST http://localhost:8080/api/v1/author/write \
-H "Authorization: Bearer <your_jwt_token>" \
-H "Content-Type: application/json" \
-d '{
      "title": "Future Post",
      "excerpt": "This post will be published in the future.",
      "content": "<p>Content of the scheduled post.</p>",
      "categories": [1],
      "visibility": "SCHEDULED",
      "scheduledPublishAt": "2025-12-01T09:00:00"
    }'
```

### Create a Draft Post

```bash
curl -X POST http://localhost:8080/api/v1/author/write \
-H "Authorization: Bearer <your_jwt_token>" \
-H "Content-Type: application/json" \
-d '{
      "title": "Draft Post",
      "excerpt": "Work in progress...",
      "content": "<p>Not ready to publish yet.</p>",
      "categories": [3],
      "visibility": "DRAFT"
    }'
```

## Notes

- **Title uniqueness:** Post titles must be unique (case-insensitive). If a duplicate title is detected, a random suffix will be appended to the slug.
- **Slug generation:** The post slug is automatically generated from the title using URL-friendly format.
- **Categories:** At least one category ID is required. Invalid category IDs will result in a 404 error.
- **Notifications:** When a post is published (visibility = `PUBLISHED`), the author will receive a notification, and a real-time notification will be broadcast to other users.
- **Date handling:** 
  - For `SCHEDULED` posts, `scheduledPublishAt` is **required** and must be in the future.
  - For `PUBLISHED` posts, `publishedAt` is optional and defaults to the current time.
