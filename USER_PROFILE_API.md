# User Profile API với Roles

## Tổng quan
API này cung cấp thông tin chi tiết về user profile bao gồm roles, thống kê và thông tin cá nhân.

## Endpoints

### 1. GET /api/v1/auth/me
Lấy thông tin profile của user hiện tại (yêu cầu authentication).

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Response:**
```json
{
  "id": "user-uuid",
  "username": "john_doe",
  "email": "john@example.com",
  "avatar": "avatar-url",
  "roles": ["ROLE_USER", "ROLE_AUTHOR"],
  "postsCount": 15,
  "savedPostsCount": 8,
  "commentsCount": 42
}
```

### 2. GET /api/v1/auth/profile/{userId}
Lấy thông tin profile của user khác (public access).

**Response:**
```json
{
  "id": "user-uuid",
  "username": "jane_doe",
  "email": "jane@example.com",
  "avatar": "avatar-url",
  "roles": ["ROLE_USER"],
  "postsCount": 5,
  "savedPostsCount": 12,
  "commentsCount": 23
}
```

## Cách sử dụng

### Frontend JavaScript

#### Lấy thông tin user hiện tại:
```javascript
const getCurrentUser = async () => {
  const token = localStorage.getItem('token');
  
  const response = await fetch('/api/v1/auth/me', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  if (response.ok) {
    const userProfile = await response.json();
    console.log('User roles:', userProfile.roles);
    console.log('Posts count:', userProfile.postsCount);
    
    // Kiểm tra roles
    const isAdmin = userProfile.roles.includes('ROLE_ADMIN');
    const isAuthor = userProfile.roles.includes('ROLE_AUTHOR');
    
    // Hiển thị UI dựa trên roles
    if (isAdmin) {
      showAdminPanel();
    }
    
    if (isAuthor) {
      showCreatePostButton();
    }
    
    return userProfile;
  }
};
```

#### Lấy thông tin user khác:
```javascript
const getUserProfile = async (userId) => {
  const response = await fetch(`/api/v1/auth/profile/${userId}`);
  
  if (response.ok) {
    const userProfile = await response.json();
    return userProfile;
  }
};
```

### Backend Service Usage

```java
@Service
public class SomeService {
    
    @Autowired
    private UserProfileService userProfileService;
    
    public void handleUserAction(UserDetailsImpl userDetails) {
        // Lấy profile với thống kê
        UserProfileResponse profile = userProfileService.getUserProfile(userDetails);
        
        // Kiểm tra roles
        List<String> roles = profile.getRoles();
        boolean isAdmin = roles.contains("ROLE_ADMIN");
        
        // Logic dựa trên roles và thống kê
        if (isAdmin && profile.getPostsCount() > 10) {
            // Admin với nhiều posts
        }
    }
}
```

## Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | User ID (UUID) |
| `username` | String | Tên đăng nhập |
| `email` | String | Email address |
| `avatar` | String | URL avatar (có thể null) |
| `roles` | Array<String> | Danh sách roles của user |
| `postsCount` | Long | Số lượng bài viết đã tạo |
| `savedPostsCount` | Long | Số lượng bài viết đã lưu |
| `commentsCount` | Long | Số lượng comments đã tạo |

## Roles trong hệ thống

- **ROLE_USER**: User cơ bản
- **ROLE_AUTHOR**: Có thể tạo và quản lý bài viết
- **ROLE_ADMIN**: Quyền quản trị toàn hệ thống

## Error Handling

### 401 Unauthorized (cho /me endpoint):
```json
"Unauthorized"
```

### 404 Not Found (cho /profile/{userId} endpoint):
```json
{
  "error": "User not found"
}
```

## Ví dụ sử dụng trong React

```jsx
import { useState, useEffect } from 'react';

const UserProfile = () => {
  const [userProfile, setUserProfile] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchUserProfile = async () => {
      try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/v1/auth/me', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok) {
          const profile = await response.json();
          setUserProfile(profile);
        }
      } catch (error) {
        console.error('Error fetching user profile:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchUserProfile();
  }, []);

  if (loading) return <div>Loading...</div>;

  return (
    <div>
      <h1>Welcome, {userProfile?.username}!</h1>
      <p>Email: {userProfile?.email}</p>
      <p>Roles: {userProfile?.roles.join(', ')}</p>
      
      <div className="stats">
        <div>Posts: {userProfile?.postsCount}</div>
        <div>Saved: {userProfile?.savedPostsCount}</div>
        <div>Comments: {userProfile?.commentsCount}</div>
      </div>
      
      {userProfile?.roles.includes('ROLE_ADMIN') && (
        <button>Admin Panel</button>
      )}
      
      {userProfile?.roles.includes('ROLE_AUTHOR') && (
        <button>Create Post</button>
      )}
    </div>
  );
};
```

## Lưu ý

1. **Authentication**: Endpoint `/me` yêu cầu JWT token trong Authorization header
2. **Public Access**: Endpoint `/profile/{userId}` có thể truy cập công khai
3. **Statistics**: Các thống kê được tính real-time từ database
4. **Roles Format**: Roles luôn có prefix "ROLE_" (ví dụ: "ROLE_USER", "ROLE_ADMIN")
5. **Error Handling**: Luôn kiểm tra response status trước khi xử lý data
