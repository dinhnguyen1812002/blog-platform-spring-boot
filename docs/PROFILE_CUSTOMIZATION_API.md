# Profile Customization API Documentation

## Overview

The **ProfileCustomizationController** provides comprehensive endpoints for managing user profiles in the Blog Platform. It allows authenticated users to customize their profiles with markdown content, update personal information, upload avatars, and manage social media links.

**Base URL:** `/api/v1/profile`

**Authentication:** Most endpoints require JWT authentication via `Authorization: Bearer <token>` header.

---

## Table of Contents

1. [Update Custom Profile Markdown](#1-update-custom-profile-markdown)
2. [Get Custom Profile by Username](#2-get-custom-profile-by-username)
3. [Update User Profile (Full)](#3-update-user-profile-full)
4. [Patch User Profile (Partial)](#4-patch-user-profile-partial)
5. [Upload Avatar](#5-upload-avatar)
6. [Get Current User Profile](#6-get-current-user-profile)
7. [Get User Profile by ID](#7-get-user-profile-by-id)
8. [Response Models](#response-models)
9. [Error Handling](#error-handling)
10. [Usage Examples](#usage-examples)

---

## Endpoints

### 1. Update Custom Profile Markdown

**Endpoint:** `PUT /api/v1/profile`

**Authentication:** Required (JWT)

**Description:**
Updates the custom profile markdown content for the authenticated user. This allows users to create a rich, formatted profile description using Markdown syntax.

**Request Headers:**
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "markdownContent": "# Welcome to My Profile\n\nI am a passionate developer...\n\n## Skills\n- Java\n- Spring Boot\n- React"
}
```

**Request Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| markdownContent | String | Yes | Markdown formatted content for the profile. Must not be blank. |

**Response:** `200 OK`
```json
{
  "id": "user-123",
  "username": "john_doe",
  "email": "john@example.com",
  "avatar": "https://example.com/avatars/user-123.jpg",
  "roles": ["ROLE_USER"],
  "socialMediaLinks": {
    "GITHUB": "https://github.com/johndoe",
    "TWITTER": "https://twitter.com/johndoe"
  },
  "postsCount": 15,
  "savedPostsCount": 42,
  "commentsCount": 87,
  "customProfileMarkdown": "# Welcome to My Profile\n\nI am a passionate developer...\n\n## Skills\n- Java\n- Spring Boot\n- React"
}
```

**Status Codes:**
- `200 OK` - Profile markdown updated successfully
- `400 Bad Request` - Invalid request body (e.g., blank markdown content)
- `401 Unauthorized` - Missing or invalid JWT token
- `404 Not Found` - User not found

**Validation Rules:**
- `markdownContent` must not be blank
- Markdown content can include headers, lists, code blocks, links, images, etc.

**Example cURL:**
```bash
curl -X PUT http://localhost:8080/api/v1/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "markdownContent": "# My Profile\n\nSoftware Engineer with 5+ years experience"
  }'
```

---

### 2. Get Custom Profile by Username

**Endpoint:** `GET /api/v1/profile/{username}`

**Authentication:** Not required (Public endpoint)

**Description:**
Retrieves the custom profile information for a specific user by their username. This endpoint is public and can be accessed by anyone to view user profiles.

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| username | String | Yes | The username of the user whose profile to retrieve |

**Response:** `200 OK`
```json
{
  "id": "user-123",
  "username": "john_doe",
  "email": "john@example.com",
  "avatar": "https://example.com/avatars/user-123.jpg",
  "roles": ["ROLE_USER"],
  "socialMediaLinks": {
    "GITHUB": "https://github.com/johndoe",
    "TWITTER": "https://twitter.com/johndoe"
  },
  "postsCount": 15,
  "savedPostsCount": 42,
  "commentsCount": 87,
  "customProfileMarkdown": "# Welcome to My Profile\n\nI am a passionate developer..."
}
```

**Status Codes:**
- `200 OK` - Profile retrieved successfully
- `404 Not Found` - User with specified username not found

**Example cURL:**
```bash
curl -X GET http://localhost:8080/api/v1/profile/john_doe
```

**Example JavaScript/Fetch:**
```javascript
fetch('http://localhost:8080/api/v1/profile/john_doe')
  .then(response => response.json())
  .then(data => console.log(data))
  .catch(error => console.error('Error:', error));
```

---

### 3. Update User Profile (Full)

**Endpoint:** `PUT /api/v1/profile/profile`

**Authentication:** Required (JWT)

**Description:**
Performs a full update of the authenticated user's profile information. All provided fields will be updated. This is a complete replacement operation for the specified fields.

**Request Headers:**
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "john_doe_updated",
  "email": "newemail@example.com",
  "avatar": "https://example.com/avatars/new-avatar.jpg",
  "bio": "Passionate developer and tech enthusiast",
  "website": "https://johndoe.dev",
  "customInformation": "Available for freelance projects",
  "socialMediaLinks": {
    "GITHUB": "https://github.com/johndoe",
    "TWITTER": "https://twitter.com/johndoe",
    "LINKEDIN": "https://linkedin.com/in/johndoe"
  }
}
```

**Request Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| username | String | No | New username (min 3 characters) |
| email | String | No | New email address (must be valid email format) |
| avatar | String | No | Avatar URL or path |
| bio | String | No | User biography |
| website | String | No | Personal website URL |
| customInformation | String | No | Custom information field |
| socialMediaLinks | Map | No | Social media platform URLs (GITHUB, TWITTER, LINKEDIN, FACEBOOK, INSTAGRAM, etc.) |

**Response:** `200 OK`
```json
{
  "id": "user-123",
  "username": "john_doe_updated",
  "email": "newemail@example.com",
  "avatar": "https://example.com/avatars/new-avatar.jpg",
  "roles": ["ROLE_USER"],
  "socialMediaLinks": {
    "GITHUB": "https://github.com/johndoe",
    "TWITTER": "https://twitter.com/johndoe",
    "LINKEDIN": "https://linkedin.com/in/johndoe"
  },
  "postsCount": 15,
  "savedPostsCount": 42,
  "commentsCount": 87,
  "customProfileMarkdown": "# Welcome to My Profile\n\nI am a passionate developer..."
}
```

**Status Codes:**
- `200 OK` - Profile updated successfully
- `400 Bad Request` - Invalid request data (e.g., invalid email format, username too short)
- `401 Unauthorized` - Missing or invalid JWT token
- `404 Not Found` - User not found
- `409 Conflict` - Username or email already exists

**Validation Rules:**
- `username` must be at least 3 characters long
- `email` must be a valid email format
- `username` and `email` must be unique across the system
- Social media links must be valid URLs

**Example cURL:**
```bash
curl -X PUT http://localhost:8080/api/v1/profile/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe_updated",
    "email": "newemail@example.com",
    "bio": "Passionate developer",
    "website": "https://johndoe.dev",
    "socialMediaLinks": {
      "GITHUB": "https://github.com/johndoe",
      "TWITTER": "https://twitter.com/johndoe"
    }
  }'
```

---

### 4. Patch User Profile (Partial)

**Endpoint:** `PATCH /api/v1/profile/profile`

**Authentication:** Required (JWT)

**Description:**
Performs a partial update of the authenticated user's profile. Only the fields provided in the request body will be updated; other fields remain unchanged. This is useful for updating specific profile attributes without affecting others.

**Request Headers:**
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body (Example - Update only bio and website):**
```json
{
  "bio": "Updated bio text",
  "website": "https://newwebsite.com"
}
```

**Request Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| username | String | No | New username (min 3 characters) |
| email | String | No | New email address (must be valid email format) |
| avatar | String | No | Avatar URL or path |
| bio | String | No | User biography |
| website | String | No | Personal website URL |
| customInformation | String | No | Custom information field |
| socialMediaLinks | Map | No | Social media platform URLs |

**Response:** `200 OK`
```json
{
  "id": "user-123",
  "username": "john_doe",
  "email": "john@example.com",
  "avatar": "https://example.com/avatars/user-123.jpg",
  "roles": ["ROLE_USER"],
  "socialMediaLinks": {
    "GITHUB": "https://github.com/johndoe"
  },
  "postsCount": 15,
  "savedPostsCount": 42,
  "commentsCount": 87,
  "customProfileMarkdown": "# Welcome to My Profile\n\nI am a passionate developer..."
}
```

**Status Codes:**
- `200 OK` - Profile patched successfully
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Missing or invalid JWT token
- `404 Not Found` - User not found
- `409 Conflict` - Username or email already exists

**Difference between PUT and PATCH:**
- **PUT** (`/api/v1/profile/profile`): Full update - all fields in request are updated
- **PATCH** (`/api/v1/profile/profile`): Partial update - only provided fields are updated

**Example cURL:**
```bash
curl -X PATCH http://localhost:8080/api/v1/profile/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "bio": "Updated bio text",
    "website": "https://newwebsite.com"
  }'
```

---

### 5. Upload Avatar

**Endpoint:** `POST /api/v1/profile/avatar`

**Authentication:** Required (JWT)

**Description:**
Uploads a new avatar image for the authenticated user. The file is processed and stored, and a URL to the uploaded avatar is returned.

**Request Headers:**
```
Authorization: Bearer <jwt_token>
Content-Type: multipart/form-data
```

**Request Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| file | File | Yes | Image file to upload (multipart form data) |

**Supported File Types:**
- JPEG (.jpg, .jpeg)
- PNG (.png)
- GIF (.gif)
- WebP (.webp)

**Response:** `200 OK`
```json
{
  "url": "https://example.com/avatars/user-123-avatar.jpg"
}
```

**Status Codes:**
- `200 OK` - Avatar uploaded successfully
- `400 Bad Request` - Invalid file format or file too large
- `401 Unauthorized` - Missing or invalid JWT token
- `404 Not Found` - User not found

**File Constraints:**
- Maximum file size: 5MB (configurable)
- Supported formats: JPEG, PNG, GIF, WebP
- Image dimensions: Recommended 200x200px or larger

**Example cURL:**
```bash
curl -X POST http://localhost:8080/api/v1/profile/avatar \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -F "file=@/path/to/avatar.jpg"
```

**Example JavaScript/Fetch:**
```javascript
const formData = new FormData();
formData.append('file', fileInput.files[0]);

fetch('http://localhost:8080/api/v1/profile/avatar', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer ' + token
  },
  body: formData
})
  .then(response => response.json())
  .then(data => console.log('Avatar URL:', data.url))
  .catch(error => console.error('Error:', error));
```

---

### 6. Get Current User Profile

**Endpoint:** `GET /api/v1/profile/profile`

**Authentication:** Required (JWT)

**Description:**
Retrieves the complete profile information for the currently authenticated user. This endpoint returns all profile details including custom markdown, statistics, and social media links.

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response:** `200 OK`
```json
{
  "id": "user-123",
  "username": "john_doe",
  "email": "john@example.com",
  "avatar": "https://example.com/avatars/user-123.jpg",
  "roles": ["ROLE_USER"],
  "socialMediaLinks": {
    "GITHUB": "https://github.com/johndoe",
    "TWITTER": "https://twitter.com/johndoe"
  },
  "postsCount": 15,
  "savedPostsCount": 42,
  "commentsCount": 87,
  "customProfileMarkdown": "# Welcome to My Profile\n\nI am a passionate developer..."
}
```

**Status Codes:**
- `200 OK` - Profile retrieved successfully
- `401 Unauthorized` - Missing or invalid JWT token
- `404 Not Found` - User not found

**Example cURL:**
```bash
curl -X GET http://localhost:8080/api/v1/profile/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Example JavaScript/Fetch:**
```javascript
fetch('http://localhost:8080/api/v1/profile/profile', {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer ' + token
  }
})
  .then(response => response.json())
  .then(data => console.log('Current user profile:', data))
  .catch(error => console.error('Error:', error));
```

---

### 7. Get User Profile by ID

**Endpoint:** `GET /api/v1/profile/profile/{userId}`

**Authentication:** Not required (Public endpoint)

**Description:**
Retrieves the profile information for a specific user by their user ID. This endpoint is public and can be accessed by anyone to view user profiles.

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | String | Yes | The unique identifier of the user |

**Response:** `200 OK`
```json
{
  "id": "user-123",
  "username": "john_doe",
  "email": "john@example.com",
  "avatar": "https://example.com/avatars/user-123.jpg",
  "roles": ["ROLE_USER"],
  "socialMediaLinks": {
    "GITHUB": "https://github.com/johndoe",
    "TWITTER": "https://twitter.com/johndoe"
  },
  "postsCount": 15,
  "savedPostsCount": 42,
  "commentsCount": 87,
  "customProfileMarkdown": "# Welcome to My Profile\n\nI am a passionate developer..."
}
```

**Status Codes:**
- `200 OK` - Profile retrieved successfully
- `404 Not Found` - User with specified ID not found

**Example cURL:**
```bash
curl -X GET http://localhost:8080/api/v1/profile/profile/user-123
```

**Example JavaScript/Fetch:**
```javascript
fetch('http://localhost:8080/api/v1/profile/profile/user-123')
  .then(response => response.json())
  .then(data => console.log('User profile:', data))
  .catch(error => console.error('Error:', error));
```

---

## Response Models

### UserProfileResponse

Complete user profile information returned by most endpoints.

```json
{
  "id": "string (UUID)",
  "username": "string",
  "email": "string (email format)",
  "avatar": "string (URL)",
  "roles": ["array of strings"],
  "socialMediaLinks": {
    "GITHUB": "string (URL)",
    "TWITTER": "string (URL)",
    "LINKEDIN": "string (URL)",
    "FACEBOOK": "string (URL)",
    "INSTAGRAM": "string (URL)"
  },
  "postsCount": "number",
  "savedPostsCount": "number",
  "commentsCount": "number",
  "customProfileMarkdown": "string (markdown formatted)"
}
```

**Field Descriptions:**
| Field | Type | Description |
|-------|------|-------------|
| id | String | Unique user identifier (UUID) |
| username | String | User's display name |
| email | String | User's email address |
| avatar | String | URL to user's avatar image |
| roles | Array | List of user roles (e.g., ROLE_USER, ROLE_ADMIN) |
| socialMediaLinks | Map | Social media platform URLs |
| postsCount | Number | Total number of posts created by user |
| savedPostsCount | Number | Total number of posts saved by user |
| commentsCount | Number | Total number of comments made by user |
| customProfileMarkdown | String | User's custom profile description in Markdown format |

### AvatarUploadResponse

Response returned after successful avatar upload.

```json
{
  "url": "string (URL to uploaded avatar)"
}
```

---

## Error Handling

### Common Error Responses

#### 400 Bad Request
```json
{
  "status": 400,
  "message": "Invalid request data",
  "errors": {
    "email": "Please provide a valid email",
    "username": "Username must have at least 3 characters"
  }
}
```

#### 401 Unauthorized
```json
{
  "status": 401,
  "message": "Unauthorized - Missing or invalid JWT token"
}
```

#### 404 Not Found
```json
{
  "status": 404,
  "message": "User not found"
}
```

#### 409 Conflict
```json
{
  "status": 409,
  "message": "Username or email already exists"
}
```

---

## Usage Examples

### Complete Profile Setup Workflow

#### Step 1: Update Profile Information
```bash
curl -X PUT http://localhost:8080/api/v1/profile/profile \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "bio": "Software Engineer",
    "website": "https://johndoe.dev",
    "socialMediaLinks": {
      "GITHUB": "https://github.com/johndoe",
      "TWITTER": "https://twitter.com/johndoe"
    }
  }'
```

#### Step 2: Upload Avatar
```bash
curl -X POST http://localhost:8080/api/v1/profile/avatar \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@avatar.jpg"
```

#### Step 3: Add Custom Profile Markdown
```bash
curl -X PUT http://localhost:8080/api/v1/profile \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "markdownContent": "# Welcome to My Profile\n\n## About Me\nI am a passionate software engineer with 5+ years of experience.\n\n## Skills\n- Java\n- Spring Boot\n- React\n- PostgreSQL"
  }'
```

#### Step 4: Retrieve Complete Profile
```bash
curl -X GET http://localhost:8080/api/v1/profile/profile \
  -H "Authorization: Bearer $TOKEN"
```

### Frontend Integration Example (React)

```javascript
import React, { useState, useEffect } from 'react';

function UserProfile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const token = localStorage.getItem('authToken');

  useEffect(() => {
    // Fetch current user profile
    fetch('http://localhost:8080/api/v1/profile/profile', {
      headers: { 'Authorization': `Bearer ${token}` }
    })
      .then(res => res.json())
      .then(data => {
        setProfile(data);
        setLoading(false);
      });
  }, [token]);

  const handleAvatarUpload = async (file) => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch('http://localhost:8080/api/v1/profile/avatar', {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${token}` },
      body: formData
    });
    const data = await response.json();
    setProfile({ ...profile, avatar: data.url });
  };

  const handleProfileUpdate = async (updates) => {
    const response = await fetch('http://localhost:8080/api/v1/profile/profile', {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(updates)
    });
    const data = await response.json();
    setProfile(data);
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="profile">
      <img src={profile.avatar} alt={profile.username} />
      <h1>{profile.username}</h1>
      <p>{profile.bio}</p>
      <div className="stats">
        <span>Posts: {profile.postsCount}</span>
        <span>Comments: {profile.commentsCount}</span>
      </div>
    </div>
  );
}

export default UserProfile;
```

---

## Security Considerations

1. **Authentication:** All write operations (PUT, PATCH, POST) require valid JWT authentication
2. **Authorization:** Users can only modify their own profile
3. **Input Validation:** All inputs are validated server-side
4. **File Upload:** Avatar uploads are validated for file type and size
5. **Email Uniqueness:** Email addresses must be unique across the system
6. **Username Uniqueness:** Usernames must be unique across the system

---

## Rate Limiting

To prevent abuse, the following rate limits are recommended:
- Profile updates: 10 requests per minute per user
- Avatar uploads: 5 requests per minute per user
- Profile reads: 100 requests per minute per user

---

## Changelog

### Version 1.0 (Current)
- Initial release with full profile customization features
- Support for markdown profile content
- Avatar upload functionality
- Social media links management
- Profile statistics (posts, comments, saved posts)

---

## Support

For issues or questions regarding the Profile Customization API, please contact the development team or create an issue in the project repository.
