# User profile management

The current user can read their profile with `GET /api/v1/profile`, update profile fields with `PATCH /api/v1/profile`, update custom markdown with `PUT /api/v1/profile`, and upload an avatar with `POST /api/v1/profile/avatar`. All require authentication.

Public profiles are available through `GET /api/v1/users/profile/{slug}`. Detailed profiles for other users are available to administrators through `GET /api/v1/admin/users/{userId}/profile`.

The legacy `GET /api/v1/user/profile/{userId}` endpoint was removed; clients must migrate to the public slug endpoint or the administrator endpoint as appropriate. See [Profile API](PROFILE_CUSTOMIZATION_API.md) for examples.
