# Role Fix Instructions

I've identified the issue with your JWT tokens not containing roles. The problem is that your users don't have any roles assigned in the database, or the roles don't have the required "ROLE_" prefix.

## Steps to Fix

1. **Restart your application** to apply the security configuration changes

2. **Access the debug endpoints** to diagnose and fix the issue:

   ```
   GET http://localhost:8888/api/debug/roles
   ```
   This will show all roles in your database.

   ```
   GET http://localhost:8888/api/debug/users
   ```
   This will show all users and their assigned roles.

   ```
   POST http://localhost:8888/api/debug/fix-roles
   ```
   This will:
   - Create ROLE_USER, ROLE_AUTHOR, and ROLE_ADMIN if they don't exist
   - Fix any existing roles without the ROLE_ prefix
   - Assign ROLE_USER to any users without roles

3. **Test your JWT token** after logging in:

   ```
   POST http://localhost:8888/api/debug/decode-token?token=your_jwt_token
   ```
   Replace `your_jwt_token` with the actual JWT token you receive after login.

## What Was Fixed

1. Added proper debugging to UserDetailsImpl.build() to handle null roles
2. Created a DebugController with endpoints to diagnose and fix role issues
3. Updated SecurityConfig to allow access to debug endpoints
4. Fixed the AuthService to look for roles with the "ROLE_" prefix

## Verification

After running the fix and logging in again, check your logs. You should now see:
```
Generating JWT for user: [username]
User authorities: [SimpleGrantedAuthority [authority=ROLE_USER]]
Roles string: 'ROLE_USER'
```

This confirms that roles are now properly assigned and included in your JWT token.