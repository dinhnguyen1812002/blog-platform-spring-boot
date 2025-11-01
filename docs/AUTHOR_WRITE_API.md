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

| Header        | Value              | Description                               |
| ------------- | ------------------ | ----------------------------------------- |
| `Authorization` | `Bearer <JWT_TOKEN>` | The JWT token for authenticating the user. |
| `Content-Type`  | `application/json` | The content type of the request body.     |

### Body

The request body should be a JSON object with the following properties:

```json
{
  "title": "Your Post Title",
  "content": "The full content of your post.",
  "thumbnail": "URL_of_the_thumbnail_image",
  "categories": ["", ""],
  "tags": ["", ""],
  "featured": false,
  "public_date": "2025-09-01T10:00:00"
}
```

### Body Parameters

| Parameter    | Type      | Required | Description                                                                                               |
| ------------ | --------- | -------- | --------------------------------------------------------------------------------------------------------- |
| `title`      | `String`  | Yes      | The title of the post.                                                                                    |
| `content`    | `String`  | Yes      | The main content of the post, can be in HTML or Markdown format.                                          |
| `thumbnail`  | `String`  | No       | The URL of the post's thumbnail image. This should be obtained from the `/api/v1/upload` endpoint.        |
| `categories` | `List<String>` | No       | A list of category names to associate with the post.                                                      |
| `tags`       | `List<String>` | No       | A list of tag names to associate with the post.                                                           |
| `featured`   | `boolean` | No       | Whether the post should be marked as "featured". Defaults to `false`.                                     |
| `public_date`| `String`  | No       | The date and time when the post should be published, in ISO-8601 format (e.g., `2025-09-01T10:00:00`). If not provided, the post is published immediately. |

## Responses

### Success Response

- **Status Code:** `201 Created`
- **Content:** A JSON object with a success message.

```json
{
  "message": "Post created successfully"
}
```

### Error Responses

- **Status Code:** `400 Bad Request`
  - **Content:** If the request body is invalid (e.g., missing `title` or `content`).
  ```json
  {
    "message": "Post title is required"
  }
  ```

- **Status Code:** `401 Unauthorized`
  - **Content:** If the user is not authenticated or the JWT token is invalid.
  ```json
  {
    "message": "User not authenticated or invalid authentication type"
  }
  ```

- **Status Code:** `500 Internal Server Error`
  - **Content:** If an unexpected error occurs on the server.
  ```json
  {
    "message": "Error creating post: <error_details>"
  }
  ```

## Example cURL Request

```bash
curl -X POST http://localhost:8080/api/v1/author/write \
-H "Authorization: Bearer <your_jwt_token>" \
-H "Content-Type: application/json" \
-d '{ \
      "title": "My First Post", \
      "content": "<h1>Hello World!</h1><p>This is my first post.</p>", \
      "thumbnail": "http://example.com/uploads/my-thumbnail.jpg", \
      "categories": ["Introduction"], \
      "tags": ["welcome", "first-post"], \
      "public_date": "2025-09-01T10:00:00" \
    }'
```
