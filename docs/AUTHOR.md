# Author API Guide

This guide provides detailed instructions for authors to manage their posts, including creating, updating, deleting, and retrieving posts.

## 1. Create a New Post

This endpoint allows an authenticated author to create a new post. The workflow involves two main steps:

1.  **Upload Thumbnail (Optional):** If the post has a thumbnail, first upload the image file to the `/api/v1/upload` endpoint. This will return a URL for the thumbnail.
2.  **Create Post:** Call this endpoint with the post details, including the thumbnail URL obtained in the previous step.

-   **Method:** `POST`
-   **URL:** `http://localhost:8888/api/v1/author/write`
-   **Authorization:** Required (Author role).
  -   **Body:** `raw` - `JSON`
    
      ```json
      {
      "title": "test 1hfdhffh",
      "content": "tetst 123 alo alo ",
      "categories": [1],
      "tags":["c4408bd9-3934-44d6-b8f2-26d30ce152c1"],
      "thumbnail": "http://localhost:8888/uploads/thumbnail/638902163329370000_wallpaper2.jpg"
      }
      ```

-   **Success Response:**

    -   **Code:** `201 Created`
    -   **Content:**
        ```json
        {
          "message": "Post created successfully"
        }
        ```

-   **Error Responses:**

    -   **Code:** `400 Bad Request` - If the request body is invalid (e.g., missing title or content).
    -   **Code:** `401 Unauthorized` - If the user is not authenticated.

## 2. Get Author's Posts

-   **Method:** `GET`
-   **URL:** `http://localhost:8888/api/v1/author/posts`
-   **Authorization:** Required.
-   **Query Parameters:**
    -   `page` (integer, optional, default: 0): The page number to retrieve.
    -   `size` (integer, optional, default: 10): The number of items per page.

## 3. Update a Post

-   **Method:** `PUT`
-   **URL:** `http://localhost:8888/api/v1/author/{id}`
-   **Authorization:** Required.
-   **Path Variable:**
    -   `id` (string): The unique identifier of the post to update.
-   **Body:** `raw` - `JSON` (Same as create post)

## 4. Delete a Post

-   **Method:** `DELETE`
-   **URL:** `http://localhost:8888/api/v1/author/{postId}`
-   **Authorization:** Required.
-   **Path Variable:**
    -   `postId` (string): The unique identifier of the post to delete.

## 5. Get Post Detail

-   **Method:** `GET`
-   **URL:** `http://localhost:8888/api/v1/author/{postId}`
-   **Authorization:** Required.
-   **Path Variable:**
    -   `postId` (string): The unique identifier of the post.