# JWT Implementation with Roles

## Overview
This document describes the enhanced JWT implementation in the blog platform that includes user roles in the token and provides utilities for role-based access control.

## JWT Token Structure

The JWT token contains the following claims:
- `userid`: User's unique identifier
- `username`: User's username
- `roles`: Comma-separated list of user roles (e.g., "ROLE_USER,ROLE_AUTHOR")
- `iat`: Issued at timestamp
- `exp`: Expiration timestamp

## Enhanced Features

### 1. Enhanced JwtResponse
The login response now includes complete user information:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": "user-uuid",
  "username": "john_doe",
  "email": "john@example.com",
  "roles": ["ROLE_USER", "ROLE_AUTHOR"]
}
```

### 2. JWT Utility Methods
New methods in `JwtUtils` class:
- `getUsernameFromJwtToken(String token)`: Extract username
- `getRolesFromJwtToken(String token)`: Extract roles string
- `getAllClaimsFromJwtToken(String token)`: Get all claims

### 3. JWT Token Service
New `JwtTokenService` provides high-level utilities:
- `extractUserId(String token)`: Get user ID
- `extractUsername(String token)`: Get username
- `extractRoles(String token)`: Get roles as List<String>
- `hasRole(String token, String role)`: Check specific role
- `isAdmin(String token)`: Check admin role
- `isAuthor(String token)`: Check author role
- `isUser(String token)`: Check user role

## API Endpoints

### Authentication Endpoints

#### POST /api/v1/auth/login
Login and receive JWT token with user information and roles.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": "user-uuid",
  "username": "john_doe",
  "email": "john@example.com",
  "roles": ["ROLE_USER", "ROLE_AUTHOR"]
}
```

#### GET /api/v1/auth/me
Get current user information including roles.

**Response:**
```json
{
  "id": "user-uuid",
  "username": "john_doe",
  "email": "john@example.com",
  "roles": ["ROLE_USER", "ROLE_AUTHOR"]
}
```

### JWT Utility Endpoints

#### POST /api/v1/jwt/decode
Decode a JWT token and extract information.

**Request:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response:**
```json
{
  "userId": "user-uuid",
  "username": "john_doe",
  "roles": ["ROLE_USER", "ROLE_AUTHOR"],
  "isAdmin": false,
  "isAuthor": true,
  "isUser": true,
  "tokenInfo": "User: john_doe (ID: user-uuid), Roles: [ROLE_USER, ROLE_AUTHOR]"
}
```

#### GET /api/v1/jwt/roles
Get roles from Authorization header token.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response:**
```json
{
  "username": "john_doe",
  "roles": ["ROLE_USER", "ROLE_AUTHOR"],
  "hasAdminRole": false,
  "hasAuthorRole": true,
  "hasUserRole": true
}
```

#### GET /api/v1/jwt/validate
Validate JWT token from Authorization header.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response:**
```json
{
  "valid": true,
  "userId": "user-uuid",
  "username": "john_doe",
  "roles": ["ROLE_USER", "ROLE_AUTHOR"]
}
```

## Usage Examples

### Frontend JavaScript
```javascript
// Login and store token with user info
const loginResponse = await fetch('/api/v1/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email: 'user@example.com', password: 'password' })
});

const { token, username, roles } = await loginResponse.json();
localStorage.setItem('token', token);
localStorage.setItem('userRoles', JSON.stringify(roles));

// Check if user has specific role
const hasAdminRole = roles.includes('ROLE_ADMIN');
const hasAuthorRole = roles.includes('ROLE_AUTHOR');

// Use token in subsequent requests
const response = await fetch('/api/v1/saved-posts', {
  headers: { 'Authorization': `Bearer ${token}` }
});
```

### Backend Service Usage
```java
@Service
public class SomeService {
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    public void someMethod(String token) {
        // Extract user information
        String userId = jwtTokenService.extractUserId(token);
        String username = jwtTokenService.extractUsername(token);
        List<String> roles = jwtTokenService.extractRoles(token);
        
        // Check roles
        if (jwtTokenService.isAdmin(token)) {
            // Admin-only logic
        }
        
        if (jwtTokenService.hasRole(token, "AUTHOR")) {
            // Author-specific logic
        }
    }
}
```

## Security Configuration

The security configuration has been updated to handle the new JWT endpoints:
- `/api/v1/jwt/decode` - Public access for token decoding
- `/api/v1/jwt/validate` - Public access for token validation
- `/api/v1/jwt/roles` - Authenticated access for role extraction

## Role-Based Access Control

The system supports three main roles:
- **ROLE_USER**: Basic user access
- **ROLE_AUTHOR**: Can create and manage posts
- **ROLE_ADMIN**: Full administrative access

Roles are automatically prefixed with "ROLE_" in Spring Security but the JWT utility methods handle both formats for convenience.

## Token Expiration

JWT tokens expire after 24 hours (configurable via `blog.app.jwtExpirationMs` property). The frontend should handle token refresh or redirect to login when tokens expire.
