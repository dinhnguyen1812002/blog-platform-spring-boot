-- =============================================================================
-- V9: Normalize roles + drop the unused user_like table (idempotent: safe to re-run)
--
-- Two-tier role model (Spring Security convention, used with hasRole('X')):
--   ROLE_USER  – every account; reads, interacts, writes and manages its OWN content
--   ROLE_ADMIN – manages ALL content and the whole platform
--
-- Legacy names are folded in: ADMIN → ROLE_ADMIN; everything else (USER, AUTHOR,
-- ROLE_AUTHOR, ROLE_MODERATOR, MODERATOR, ROLE_EDITOR) → ROLE_USER. Moderators are
-- deliberately NOT promoted to admin; grant ROLE_ADMIN explicitly if intended.
--
-- NOTE: some databases were created by Hibernate (ddl-auto) where roles.name has
-- no UNIQUE constraint, so duplicates are merged and the constraint is added first.
-- =============================================================================

-- 1. Merge duplicate role rows (same name) onto the lowest id
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT ur.user_id, keep.id
FROM user_roles ur
JOIN roles dup ON dup.id = ur.role_id
JOIN (SELECT name, MIN(id) AS id FROM roles GROUP BY name) keep ON keep.name = dup.name
WHERE dup.id <> keep.id;

DELETE ur FROM user_roles ur
JOIN roles dup ON dup.id = ur.role_id
JOIN (SELECT name, MIN(id) AS id FROM roles GROUP BY name) keep ON keep.name = dup.name
WHERE dup.id <> keep.id;

DELETE r FROM roles r
JOIN (SELECT name, MIN(id) AS id FROM roles GROUP BY name) keep ON keep.name = r.name
WHERE r.id <> keep.id;

-- 2. Enforce one row per role name (only if no unique index on name exists yet)
SET @has_unique := (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'roles'
      AND column_name = 'name' AND non_unique = 0
);
SET @ddl := IF(@has_unique = 0,
    'ALTER TABLE roles ADD CONSTRAINT uk_roles_name UNIQUE (name)',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. Seed canonical roles
INSERT IGNORE INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');

-- 4. Re-map users holding legacy roles onto canonical roles
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT ur.user_id, target.id
FROM user_roles ur
JOIN roles legacy ON legacy.id = ur.role_id
JOIN roles target ON target.name = CASE legacy.name
        WHEN 'ADMIN' THEN 'ROLE_ADMIN'
        ELSE 'ROLE_USER'
    END
WHERE legacy.name IN ('ADMIN', 'USER', 'AUTHOR', 'ROLE_AUTHOR', 'ROLE_MODERATOR', 'MODERATOR', 'ROLE_EDITOR');

-- 5. Every account is at least a ROLE_USER
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM user u
JOIN roles r ON r.name = 'ROLE_USER';

-- 6. Remove legacy roles (assignments first: FK may not cascade on Hibernate-made tables)
DELETE ur FROM user_roles ur
JOIN roles r ON r.id = ur.role_id
WHERE r.name IN ('ADMIN', 'USER', 'AUTHOR', 'ROLE_AUTHOR', 'ROLE_MODERATOR', 'MODERATOR', 'ROLE_EDITOR');

DELETE FROM roles
WHERE name IN ('ADMIN', 'USER', 'AUTHOR', 'ROLE_AUTHOR', 'ROLE_MODERATOR', 'MODERATOR', 'ROLE_EDITOR');

-- 7. user_like was never written: likes live in post_like (Post.likes)
DROP TABLE IF EXISTS user_like;
