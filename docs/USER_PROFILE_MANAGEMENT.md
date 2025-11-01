# User Profile Management API

This document provides a guide on how to use the API endpoints for managing user profiles, including retrieving public profiles, updating profile information, and changing a user's avatar.

---

## 1. Get Public User Profile

This endpoint retrieves a user's public profile information, which can be displayed to other users.

- **URL:** `/api/v1/user/profile/{userId}`
- **Method:** `GET`
- **Authentication:** Not required. This is a public endpoint.

### URL Parameters

| Parameter | Type   | Description                  |
| :-------- | :----- | :--------------------------- |
| `userId`  | String | The unique ID of the user.   |

### Success Response

- **Code:** `200 OK`
- **Content:** Returns a `UserProfileResponse` object containing the user's public information.

**Example `curl` command:**

```bash
curl -X GET "http://localhost:8080/api/v1/user/profile/some-user-id"
```

### Error Responses

- **Code:** `404 Not Found`
  - **Reason:** The user with the specified `userId` does not exist.

---

## 2. Update User Profile

This endpoint allows an authenticated user to update their own profile information.

- **URL:** `/api/v1/user/profile`
- **Method:** `PUT` or `PATCH`
- **Authentication:** Required. A valid Bearer Token must be included in the `Authorization` header.

### Request Body

The request body must be a JSON object containing the fields to be updated.

| Field      | Type   | Description                            | Required | 
| :--------- | :----- | :------------------------------------- | :------- |
| `username` | String | The new username.                      | No       |
| `email`    | String | The new email address.                 | No       |
| `bio`      | String | A short biography for the user profile.| No       |

**Example Request Body:**

```json
{
  "username": "new.username",
  "bio": "This is my updated bio."
}
```

### Success Response

- **Code:** `200 OK`
- **Content:** Returns the updated `UserProfileResponse` object.

### Testing with `curl`

1.  First, obtain an authentication token.
2.  Then, make the `PUT` request with your token and the request body.

```bash
# Replace <your_token> and <user_id> with actual values
TOKEN="<your_token>"

curl -X PUT "http://localhost:8080/api/v1/user/profile" \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d 
           "username": "new.username",
           "bio": "This is my updated bio."
         }'
```

### Error Responses

- **Code:** `400 Bad Request`
  - **Reason:** The request body is invalid (e.g., username already exists, email is malformed).
- **Code:** `401 Unauthorized`
  - **Reason:** The request is missing a valid authentication token.

---

## 3. Update User Avatar

This endpoint allows an authenticated user to upload a new avatar image.

- **URL:** `/api/v1/user/avatar`
- **Method:** `POST`
- **Authentication:** Required. A valid Bearer Token must be included in the `Authorization` header.

### Request Body

The request must be sent as `multipart/form-data` and contain the image file.

| Part Name | Type | Description               |
| :-------- | :--- | :------------------------ |
| `file`    | File | The avatar image to upload. |

### Success Response

- **Code:** `200 OK`
- **Content:** Returns an `AvatarUploadResponse` object containing the URL of the newly uploaded avatar.

**Example Response:**

```json
{
  "avatarUrl": "/uploads/avatars/some-unique-filename.jpg"
}
```

### Testing with `curl`

1.  First, obtain an authentication token.
2.  Then, make the `POST` request with your token and the file path.

```bash
# Replace <your_token> and /path/to/your/image.jpg with actual values
TOKEN="<your_token>"

curl -X POST "http://localhost:8080/api/v1/user/avatar" \
     -H "Authorization: Bearer $TOKEN" \
     -F "file=@/path/to/your/image.jpg"
```

### Error Responses

- **Code:** `400 Bad Request`
  - **Reason:** No file was provided in the request.
- **Code:** `401 Unauthorized`
  - **Reason:** The request is missing a valid authentication token.
- **Code:** `500 Internal Server Error`
  - **Reason:** An error occurred while saving the file on the server.
