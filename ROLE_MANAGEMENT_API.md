# Role Management API

## Tổng quan
API này cung cấp các chức năng quản lý roles và assign roles cho users trong hệ thống.

## Endpoints

### 1. Lấy tất cả roles
**GET /api/v1/roles**
```json
[
  {
    "id": 1,
    "name": "ADMIN",
    "displayName": "ADMIN",
    "userCount": 2
  },
  {
    "id": 2,
    "name": "AUTHOR", 
    "displayName": "AUTHOR",
    "userCount": 5
  },
  {
    "id": 3,
    "name": "USER",
    "displayName": "USER", 
    "userCount": 100
  }
]
```

### 2. Lấy role cụ thể với danh sách users
**GET /api/v1/roles/{roleId}**
```json
{
  "id": 3,
  "name": "USER",
  "displayName": "USER",
  "userCount": 2,
  "users": [
    {
      "id": "user-uuid-1",
      "name": "john_doe",
      "email": "john@example.com"
    },
    {
      "id": "user-uuid-2", 
      "name": "jane_doe",
      "email": "jane@example.com"
    }
  ]
}
```

### 3. Lấy role theo tên với danh sách users
**GET /api/v1/roles/name/{roleName}**
```bash
GET /api/v1/roles/name/USER
GET /api/v1/roles/name/ADMIN
GET /api/v1/roles/name/AUTHOR
```

### 4. Lấy users theo role (có phân trang)
**GET /api/v1/roles/{roleId}/users?page=0&size=10**

### 5. Assign roles cho user
**POST /api/v1/roles/assign**
```json
{
  "userId": "user-uuid",
  "roleIds": [1, 3]
}
```

### 6. Remove role từ user
**DELETE /api/v1/roles/users/{userId}/roles/{roleId}**

### 7. Lấy roles của user cụ thể
**GET /api/v1/roles/users/{userId}**

### 8. Thống kê roles
**GET /api/v1/roles/statistics**

## Cách sử dụng

### 1. Assign role USER cho user hiện tại (để test)
```bash
curl -X POST http://localhost:8080/api/v1/auth/assign-default-role \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 2. Lấy tất cả roles với số lượng users
```bash
curl -X GET http://localhost:8080/api/v1/roles \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

### 3. Lấy users có role USER
```bash
curl -X GET http://localhost:8080/api/v1/roles/3 \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

### 4. Assign multiple roles cho user
```bash
curl -X POST http://localhost:8080/api/v1/roles/assign \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-uuid",
    "roleIds": [1, 3]
  }'
```

### 5. Remove role từ user
```bash
curl -X DELETE http://localhost:8080/api/v1/roles/users/user-uuid/roles/1 \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

## Frontend Usage

### React Example
```jsx
const RoleManagement = () => {
  const [roles, setRoles] = useState([]);
  const [selectedRole, setSelectedRole] = useState(null);

  // Lấy tất cả roles
  const fetchRoles = async () => {
    const response = await fetch('/api/v1/roles', {
      headers: { 'Authorization': `Bearer ${adminToken}` }
    });
    const data = await response.json();
    setRoles(data);
  };

  // Lấy users của role cụ thể
  const fetchRoleUsers = async (roleId) => {
    const response = await fetch(`/api/v1/roles/${roleId}`, {
      headers: { 'Authorization': `Bearer ${adminToken}` }
    });
    const data = await response.json();
    setSelectedRole(data);
  };

  // Assign role cho user
  const assignRole = async (userId, roleIds) => {
    const response = await fetch('/api/v1/roles/assign', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ userId, roleIds })
    });
    
    if (response.ok) {
      alert('Roles assigned successfully!');
      fetchRoles(); // Refresh data
    }
  };

  return (
    <div>
      <h2>Role Management</h2>
      
      {/* Hiển thị danh sách roles */}
      <div>
        <h3>Roles</h3>
        {roles.map(role => (
          <div key={role.id} onClick={() => fetchRoleUsers(role.id)}>
            <strong>{role.displayName}</strong> ({role.userCount} users)
          </div>
        ))}
      </div>

      {/* Hiển thị users của role được chọn */}
      {selectedRole && (
        <div>
          <h3>Users with {selectedRole.displayName} role</h3>
          {selectedRole.users.map(user => (
            <div key={user.id}>
              {user.name} ({user.email})
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
```

## Quy trình để fix vấn đề roles

### Bước 1: Assign role USER cho user hiện tại
```bash
curl -X POST http://localhost:8080/api/v1/auth/assign-default-role \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Bước 2: Test lại API /me
```bash
curl -X GET http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Bước 3: Nếu cần assign thêm roles khác (cần ADMIN token)
```bash
curl -X POST http://localhost:8080/api/v1/roles/assign \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "your-user-id",
    "roleIds": [1, 2, 3]
  }'
```

## Security
- Tất cả endpoints role management yêu cầu ADMIN role
- Endpoint assign-default-role chỉ cần authentication
- Endpoints public: không có

## Database Schema
```sql
-- Roles table
CREATE TABLE roles (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(20) NOT NULL
);

-- User roles junction table  
CREATE TABLE user_roles (
  user_id VARCHAR(255) NOT NULL,
  role_id INT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  FOREIGN KEY (user_id) REFERENCES user(id),
  FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

## Lưu ý
1. Cần có ADMIN role để sử dụng hầu hết các endpoints
2. Endpoint assign-default-role giúp assign role USER cho user hiện tại
3. Sau khi assign role, cần login lại để JWT token được refresh với roles mới
4. Roles được lưu trong JWT token với prefix "ROLE_" (ví dụ: "ROLE_USER")
