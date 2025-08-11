# Bookmarks API Guide

This guide provides detailed instructions for testing the Bookmarks API, which allows users to save, manage, and retrieve their bookmarked posts.

## 1. Bookmark a Post

-   **Method:** `POST`
-   **URL:** `http://localhost:8888/api/v1/post/{postId}/bookmark`
-   **Authorization:** Required.
-   **Path Variable:**
    -   `postId` (string): The unique identifier of the post to be bookmarked.
-   **Body:** (Optional) `raw` - `JSON`

    You can optionally include notes when bookmarking a post.

    ```json
    {
      "notes": "This is an interesting article about..."
    }
    ```

## 2. Unbookmark a Post

-   **Method:** `DELETE`
-   **URL:** `http://localhost:8888/api/v1/post/{postId}/bookmark`
-   **Authorization:** Required.
-   **Path Variable:**
    -   `postId` (string): The unique identifier of the post to be unbookmarked.

## 3. Get Bookmarked Posts (Paginated)

-   **Method:** `GET`
-   **URL:** `http://localhost:8888/api/v1/post/saved-posts`
-   **Authorization:** Required.
-   **Query Parameters:**
    -   `page` (integer, optional, default: 0): The page number to retrieve.
    -   `size` (integer, optional, default: 10): The number of items per page.

## 4. Get All Bookmarked Posts (List)

-   **Method:** `GET`
-   **URL:** `http://localhost:8888/api/v1/post/list`
-   **Authorization:** Required.

## 5. Check if a Post is Bookmarked

-   **Method:** `GET`
-   **URL:** `http://localhost:8888/api/v1/post/check/{postId}`
-   **Authorization:** Required.
-   **Path Variable:**
    -   `postId` (string): The unique identifier of the post to check.
-   **Response:**
    -   `true` if the post is bookmarked by the current user, `false` otherwise.

## 6. Get User's Bookmarked Posts Count

-   **Method:** `GET`
-   **URL:** `http://localhost:8888/api/v1/post/count`
-   **Authorization:** Required.
-   **Response:**
    -   A long integer representing the total number of posts bookmarked by the user.

## 7. Get Post's Bookmark Count

-   **Method:** `GET`
-   **URL:** `http://localhost:8888/api/v1/post/post/{postId}/count`
-   **Authorization:** Required.
-   **Path Variable:**
    -   `postId` (string): The unique identifier of the post.
-   **Response:**
    -   A long integer representing the total number of times the post has been bookmarked by all users.

## 8. Update Notes on a Bookmarked Post

-   **Method:** `PUT`
-   **URL:** `http://localhost:8888/api/v1/post/notes/{savedPostId}`
-   **Authorization:** Required.
-   **Path Variable:**
    -   `savedPostId` (string): The unique identifier of the saved post entry.
-   **Body:** `raw` - `JSON`

    ```json
    {
      "notes": "Updated notes for this bookmarked post."
    }
    ```