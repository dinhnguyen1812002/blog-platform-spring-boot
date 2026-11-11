# Profile API

## Public profile

`GET /api/v1/users/profile/{slug}` is public. It returns the public profile, social links, and featured posts. The response does not contain the profile email or saved-post count. A missing slug returns 404.

```bash
curl http://localhost:8080/api/v1/users/profile/john-doe
```

## Current user

These endpoints require a JWT bearer token or the authenticated session cookie:

- `GET /api/v1/profile`: retrieve the current user's detailed profile.
- `PATCH /api/v1/profile`: update the current user's profile fields.
- `PUT /api/v1/profile`: replace the current user's custom markdown with `{"markdownContent":"..."}`.
- `POST /api/v1/profile/avatar`: upload an avatar using multipart field `file`.

```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/profile
curl -X PATCH -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"bio":"My profile"}' http://localhost:8080/api/v1/profile
```

## Administrator

`GET /api/v1/admin/users/{userId}/profile` requires `ROLE_ADMIN` and returns the detailed `UserProfileResponse`. A missing user returns 404.

## Migration

The ambiguous `GET /api/v1/profile/{username}` and `GET /api/v1/profile/{userId}` routes have been removed. For public profiles, use the slug endpoint; for a detailed profile by ID, use the administrator endpoint. The legacy public `GET /api/v1/user/profile/{userId}` has also been removed. Update existing clients before deploying this change.
