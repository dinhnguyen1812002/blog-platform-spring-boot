# Flyway Migration Documentation

## Overview
This directory contains Flyway database migrations for the Spring Boot Blog Platform. The migrations define the complete database schema based on JPA entity classes.

## Database Configuration
- **Engine**: MySQL 8.x
- **Character Set**: utf8mb4 (full Unicode including emoji support)
- **Collation**: utf8mb4_unicode_ci
- **Storage Engine**: InnoDB (supports foreign keys and transactions)

## Migration Files

### V1: Standalone Tables (No Foreign Key Dependencies)
**File**: `V1__create_core_tables.sql`

**Tables Created**:
- `roles` - User roles/authorities (ROLE_USER, ROLE_ADMIN, ROLE_MODERATOR)
- `category` - Blog post categories with color coding
- `tags` - Content tags for topic organization
- `meme` - Meme template storage
- `traffic_counters` - Aggregated traffic analytics by day/month/year
- `notification_templates` - Reusable notification templates for multi-channel
- `newsletter_subscription` - Main newsletter subscription list
- `newsletter_subscribers` - Detailed subscriber management with GDPR fields

**Rationale**: These tables have no foreign key dependencies on other application tables (they reference only themselves or external enums). Creating them first avoids circular dependency issues.

---

### V2: User and Authentication Tables
**File**: `V2__create_user_and_auth_tables.sql`

**Tables Created**:
- `user` - Core user accounts with authentication data
- `social_media_links` - User social profile links (1 per platform per user)
- `refresh_tokens` - JWT refresh token storage
- `api_keys` - External API key management with hashed secrets
- `oauth_accounts` - Linked OAuth provider accounts
- `jwt_blacklist` - Revoked JWT tokens (stored as SHA-256 hashes)

**Note**: The `user_roles` join table is intentionally deferred to V4 to avoid circular dependency (requires both user and roles to exist).

**Foreign Keys**: All FKs reference `user.id` with `ON DELETE CASCADE` for automatic cleanup.

---

### V3: Content Tables (Post, Series, SeriesPost)
**File**: `V3__create_post_and_series_tables.sql`

**Tables Created**:
- `post` - Core blog articles with publishing workflow states (draft, published, private, archived)
- `series` - Grouped collections of related posts with ordering
- `series_post` - Join table linking posts to series with `order_index`

**Key Features**:
- Full-text search index on `post.title` and `post.content`
- Automated timestamp columns (`created_at`, `updated_at`)
- Visibility and publishing controls
- View count tracking for analytics

**Foreign Keys**:
- `post.user_id` → `user.id` (author)
- `series.user_id` → `user.id` (creator)
- Cascade deletes ensure cleanup of orphaned content

---

### V4: Many-to-Many Join Tables
**File**: `V4__create_join_tables.sql`

**Tables Created**:
- `user_roles` - User ↔ Role assignments
- `post_like` - Post ↔ User likes (many-to-many)
- `post_category` - Post ↔ Category associations
- `post_tags` - Post ↔ Tag associations

**Design Decisions**:
- Composite primary keys on `(col1, col2)` pairs for uniqueness
- Unique constraints prevent duplicate relationships (e.g., user liking same post twice)
- Indexes on both sides of each relationship for bidirectional query performance
- All FK constraints use `ON DELETE CASCADE` to maintain referential integrity

**Deferred Order**: These tables depend on `user`, `roles` (V1, V2) and `post`, `category`, `tags` (V3).

---

### V5: Interaction Tables
**File**: `V5__create_interaction_tables.sql`

**Tables Created**:
- `comment` - Nested/threaded comments with depth tracking
- `rating` - Star ratings (1-5) with unique constraint per user-per-post
- `bookmark` - User bookmarks/saved posts with notes

**Comment Structure**:
- Self-referential foreign key (`parent_id`) for nested replies
- `depth` column for hierarchical queries
- `reply_count` denormalized for performance

**Unique Constraints**:
- `rating`: `(user_id, post_id)` prevents duplicate ratings
- `bookmark`: `(user_id, post_id)` prevents duplicate bookmarks

**Foreign Keys**: All reference `user` and `post` with cascade deletes.

---

### V6: Notification System
**File**: `V6__create_notification_tables.sql`

**Tables Created**:
- `notifications` - User notification inbox with read status
- `user_notification_preferences` - Per-channel notification settings (email, push, SMS, in-app)
- `notification_history` - Complete audit log of all notification delivery attempts

**Notification Preferences**:
- Unique constraint: `(user_id, channel)` ensures one preference row per channel per user
- Supports quiet hours, digest mode, and channel-specific tokens

**Notification History**:
- Tracks delivery status (pending → sent → delivered → read)
- Retry logic support with `retry_count`, `max_retries`, `next_retry_at`
- Stores external message IDs for troubleshooting

---

### V7: Newsletter and Email Campaigns
**File**: `V7__create_newsletter_and_email_tables.sql`

**Tables Created**:
- `newsletter_campaigns` - Email campaign management with metrics
- `email_logs` - Individual email delivery logs per subscriber

**Campaign Analytics**:
- Tracks recipient count, sent count, opens, clicks, bounces, unsubscribes, complaints
- Batch sending configuration (`batch_size`, `send_interval_seconds`)
- UTM parameter support for tracking
- Segmentation by `target_segment` and `target_tags`

**Email Logs**:
- Links campaigns to `newsletter_subscribers` via string IDs (no FK for flexibility)
- Tracks per-email opens, clicks, bounces, complaints
- Indexes on status, timestamps for reporting queries

---

### V8: Reporting, Audit, and Media
**File**: `V8__create_reporting_and_audit_tables.sql`

**Tables Created**:
- `video` - Video content metadata
- `article_reports` - User-reported content violations with moderation workflow
- `oauth_audit_logs` - Security audit trail for OAuth events

**Article Reports**:
- Report categories: spam, inappropriate, copyright, etc.
- Status workflow: PENDING → RESOLVED / DISMISSED
- Tracks reporter, reviewed_by admin, timestamps
- Unique constraint: one user can report a specific post only once

**OAuth Audit Logs**:
- Immutable log of all OAuth authentication events
- Captures IP, user agent, success/failure, provider
- No foreign keys (auditing tables should not prevent cleanup)

---

## Migration Order Logic

The migration order follows a strict dependency graph to avoid foreign key constraint violations:

```
Level 1 (V1): Independent tables
  ├─ roles, category, tags, meme, traffic_counters
  ├─ notification_templates, newsletter_subscription, newsletter_subscribers

Level 2 (V2): User and authentication (depends on: roles for logic, not FK)
  ├─ user, social_media_links, refresh_tokens
  ├─ api_keys, oauth_accounts, jwt_blacklist

Level 3 (V3): Core content (depends on V2 user)
  ├─ post, series, series_post (series_post depends on both post and series)

Level 4 (V4): Join tables (depends on V1, V2, V3)
  ├─ user_roles (needs user + roles)
  ├─ post_like, post_category, post_tags (need post + respective parent)

Level 5 (V5): Interactions (depends on V2 + V3)
  ├─ comment, rating, bookmark (all reference user and post)

Level 6 (V6): Notifications (depends on V2 user)
  ├─ notifications, user_notification_preferences, notification_history

Level 7 (V7): Newsletters (depends on V2 user + V1 newsletter_subscribers)
  ├─ newsletter_campaigns (created_by → user)
  └─ email_logs (references campaigns and subscribers by string ID)

Level 8 (V8): Reporting/Audit (depends on V2 user, V3 post, V2 oauth_accounts)
  ├─ video (independent)
  ├─ article_reports (needs post + user)
  └─ oauth_audit_logs (independent audit trail)
```

---

## Indexing Strategy

### Primary Keys
- UUID (`VARCHAR(36)`) for distributed systems compatibility
- Auto-increment `BIGINT` for join tables and simple sequences

### Foreign Key Indexes
- Every FK column is indexed for join performance

### Unique Constraints
- Natural keys: `username`, `email`, `slug`, `api_key`, `token_hash`, etc.
- Composite uniques: `(user_id, role_id)`, `(user_id, post_id)` for bookmarks/likes

### Full-Text Search
- `post(title, content)` - MySQL full-text index for article search

### Composite Indexes for Filtering
- `notification_history(user_id, status, created_at)` - fetch unread notifications
- `post(user_id, visibility, published_at)` - user's published posts
- `email_logs(campaign_id, status)` - campaign delivery reports

---

## Data Types (MySQL 8.x)

| Java Type       | MySQL Type           | Notes                               |
|-----------------|----------------------|-------------------------------------|
| String (UUID)   | VARCHAR(36)          | UUIDs stored as 36-char strings    |
| String (short)  | VARCHAR(50-255)      | Length based on field constraints  |
| String (long)   | TEXT / LONGTEXT      | TEXT: 65KB, LONGTEXT: 4GB          |
| Long            | BIGINT               | 64-bit integer                     |
| Integer         | INT                  | 32-bit integer                     |
| Boolean         | BOOLEAN (TINYINT(1)) | MySQL treats as TINYINT(1)          |
| LocalDateTime   | TIMESTAMP            | UTC-based, auto-updates supported |
| Instant         | TIMESTAMP            | Same as LocalDateTime              |
| LocalDate       | DATE                 | Date only                          |
| Enum (String)   | ENUM(...)            | Limited set of predefined values   |

---

## Constraints and Validation

### Check Constraints
- `rating.score` ∈ [1, 5] enforced via `CHECK` constraint
- All `NOT NULL` columns explicitly defined

### Default Values
- Timestamps: `DEFAULT CURRENT_TIMESTAMP` or `NULL` (Hibernate manages)
- Booleans: `DEFAULT FALSE` (Hibernate manages)
- Counters: `DEFAULT 0`

### Cascade Rules
- **ON DELETE CASCADE**: Child records deleted when parent deleted
  - `user` → `social_media_links`, `refresh_tokens`, `api_keys`, `oauth_accounts`, `post`, `series`, `comment`, `rating`, `bookmark`, `notifications`, `user_notification_preferences`
- **ON DELETE SET NULL**: Optional relationships
  - `newsletter_campaigns.created_by` → user
  - `article_reports.reviewed_by` → user

---

## Performance Considerations

### Batching and Fetching
- Hibernate configured with `default_batch_fetch_size=20` (see application.yml)
- Lazy loading for `@ManyToOne` and `@OneToMany` relationships

### Query Optimization
- Index on all foreign key columns
- Composite indexes for common query patterns
- Full-text index on `post` for search
- `created_at` indexes for chronological queries

### Denormalization
- `post.view_count`, `post.view` (redundant, may be for separate metrics)
- `series.total_posts` - Maintained by application logic
- `comment.reply_count` - Denormalized for quick access

---

## Backward Compatibility

All migrations are written as **idempotent** scripts using `CREATE TABLE IF NOT EXISTS` to allow:
- Safe re-execution in development
- Partial migration recovery
- Schema drift detection

**Important**: Production deployments should follow standard Flyway practices:
1. Review pending migrations: `flyway info`
2. Test migrations in staging environment
3. Backup database before applying
4. Monitor migration logs for errors

---

## Flyway Configuration Reference

Add to `application.yml` or `application-prod.yml`:

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    validate-on-migrate: true
    clean-disabled: true  # NEVER enable clean in production
```

---

## Entity-to-Table Mapping Summary

| Entity Class          | Table Name             | Migration |
|----------------------|------------------------|-----------|
| Role                 | roles                  | V1        |
| Category             | category               | V1        |
| Tags                 | tags                   | V1        |
| Meme                 | meme                   | V1        |
| Traffic              | traffic_counters       | V1        |
| NotificationTemplate | notification_templates | V1        |
| Newsletter           | newsletter_subscription| V1        |
| NewsletterSubscriber | newsletter_subscribers | V1        |
| User                 | user                   | V2        |
| SocialMediaLink      | social_media_links     | V2        |
| RefreshToken         | refresh_tokens         | V2        |
| ApiKey               | api_keys               | V2        |
| OAuthAccount         | oauth_accounts         | V2        |
| JwtBlacklist         | jwt_blacklist          | V2        |
| Post                 | post                   | V3        |
| Series               | series                 | V3        |
| SeriesPost           | series_post            | V3        |
| UserRoles (join)     | user_roles             | V4        |
| PostLike (join)      | post_like              | V4        |
| PostCategory (join)  | post_category          | V4        |
| PostTags (join)      | post_tags              | V4        |
| Comment              | comment                | V5        |
| Rating               | rating                 | V5        |
| Bookmark             | bookmark               | V5        |
| Notifications        | notifications          | V6        |
| UserNotificationPrefs| user_notification_preferences | V6 |
| NotificationHistory  | notification_history   | V6        |
| NewsletterCampaign   | newsletter_campaigns   | V7        |
| EmailLog             | email_logs             | V7        |
| ArticleReport        | article_reports        | V8        |
| OAuthAuditLog        | oauth_audit_logs       | V8        |
| Video                | video                  | V8        |

---

## Notes on Design Decisions

1. **UUID Primary Keys**: Selected for distributed system compatibility and security (non-guessable). Used for most entities except where auto-increment is more practical (`roles.id`, `category.category_id`, `traffic_counters.id`, `rating.id`, `series_post.id`).

2. **No Soft Deletes**: Currently no `deleted_at` column. If soft deletion is needed, add column + index in a future migration.

3. **String-based IDs for Logs**: Tables like `email_logs`, `notification_history`, `oauth_audit_logs` store entity IDs as `VARCHAR` instead of FK constraints for:
   - Flexibility with eventual consistency
   - Retention of audit data even if referenced entity is deleted
   - Simplified integration with external systems

4. **ENUM Types**: MySQL ENUM used for limited sets: `visibility`, `status`, `period_type`, `channel`, `frequency`, `provider`, etc.
   - Pros: storage efficient, validation at DB layer
   - Cons: schema migrations needed to add values (Flyway handles this)

5. **Timestamp Management**: Mixed approach:
   - Hibernate `@CreationTimestamp` / `@UpdateTimestamp` for automatic timestamps
   - Explicit `created_at`, `updated_at` columns in most tables
   - `opened_at`, `clicked_at`, `sent_at` for engagement tracking

6. **Full-Text Search**: `post` table includes `FULLTEXT` index on `(title, content)`. For large-scale search, consider Elasticsearch integration.

7. **Cascading Deletes**: Extensive use of `ON DELETE CASCADE` ensures no orphaned records. Review carefully before deleting users (may want soft delete instead).

---

## Next Steps

1. **Verify migrations**: Run `mvn flyway:info` or `./gradlew flywayInfo`
2. **Apply migrations**: `mvn flyway:migrate` or `./gradlew flywayMigrate`
3. **Review generated schema**: Compare with Hibernate's expected model
4. **Seed data**: Consider adding data migration for default roles, admin user, notification templates
5. **Backup**: Backup production database before first migration

---

## Maintenance

### Adding New Tables
- Add to appropriate migration based on FK dependencies
- Follow naming conventions (snake_case, singular table names)
- Document indexes and constraints in comments

### Schema Changes
- Never modify existing migration files after they have been applied in any environment
- Create new migration files with descriptive names: `V9__alter_post_add_subtitle_column.sql`

### Rollbacks
- Flyway does not support automatic rollbacks
- Write manual undo scripts if needed: `U9__alter_post_drop_subtitle_column.sql`
- Test rollbacks in development environment

---

## References
- Flyway Documentation: https://flywaydb.org/documentation/
- MySQL 8.0 Reference: https://dev.mysql.com/doc/refman/8.0/en/
- Spring Boot Flyway: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.sql.flyway
