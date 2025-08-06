# API Documentation: User Profile Customization

This document provides details for the API endpoint that allows users to customize their public profile page using Markdown.

---

## Update Custom Profile

Allows an authenticated user to set or update the Markdown content for their personal profile page.

- **URL:** `/api/v1/users/profile/custom`
- **Method:** `PUT`
- **Authentication:** `Required`. A valid Bearer Token must be included in the `Authorization` header.

### Request Body

The request body must be a JSON object containing the Markdown content.

**Fields:**

| Field             | Type   | Description                                       | Required |
|-------------------|--------|---------------------------------------------------|----------|
| `markdownContent` | String | The Markdown content for the user's profile.      | Yes      |

**Example Request:**

```json
{
  "markdownContent": "# Welcome to my Profile!\n\nI am a software developer with a passion for open-source projects.\n\n- **Languages:** Java, Python, JavaScript\n- **Frameworks:** Spring Boot, React"
}
```

### Success Response

- **Code:** `200 OK`
- **Content:** Returns the complete `UserProfileResponse` object for the user, including the newly updated `customProfileMarkdown` field.

**Example Response:**

```json
{
  "id": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
  "username": "john.doe",
  "email": "john.doe@example.com",
  "avatar": "/path/to/avatar.png",
  "postCount": 15,
  "commentCount": 42,
  "customProfileMarkdown": "# Welcome to my Profile!\n\nI am a software developer with a passion for open-source projects.\n\n- **Languages:** Java, Python, JavaScript\n- **Frameworks:** Spring Boot, React"
}
```

### Error Responses

- **Code:** `400 Bad Request`
  - **Reason:** The request body is invalid. For example, the `markdownContent` exceeds the maximum allowed length.
  - **Content:** `{"message": "Nội dung profile không được vượt quá 10000 ký tự"}`

- **Code:** `401 Unauthorized`
  - **Reason:** The request was made without a valid `Authorization` header or the token is expired.

### Important Security Note: XSS Prevention

The backend stores the raw Markdown provided by the user. It is the **critical responsibility of the frontend application** to sanitize the HTML that is rendered from this Markdown content.

Failure to do so will expose your application to **Cross-Site Scripting (XSS)** vulnerabilities. We strongly recommend using a library like `DOMPurify` on the client-side after converting the Markdown to HTML.

**Example Frontend Logic (JavaScript):**

```javascript
import { marked } from 'marked';
import DOMPurify from 'dompurify';

// 1. Get the raw markdown from the API response
const rawMarkdown = userProfile.customProfileMarkdown;

// 2. Convert markdown to HTML (this step can create unsafe HTML)
const unsafeHtml = marked.parse(rawMarkdown);

// 3. Sanitize the HTML to remove any malicious scripts or tags
const safeHtml = DOMPurify.sanitize(unsafeHtml);

// 4. Only now, render the safe HTML to the page
document.getElementById('profile-display-area').innerHTML = safeHtml;
```

```