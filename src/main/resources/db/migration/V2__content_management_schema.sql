-- Migration: V2__content_management_schema.sql
-- Purpose: Create tables for categories, tags, and blog posts

-- 1. Category table
CREATE TABLE IF NOT EXISTS category (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    background_color VARCHAR(7),
    description VARCHAR(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Tags table
CREATE TABLE IF NOT EXISTS tags (
    uuid BINARY(16) PRIMARY KEY,
    name VARCHAR(255),
    slug VARCHAR(255),
    description VARCHAR(255),
    color VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Post table
CREATE TABLE IF NOT EXISTS post (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    excerpt VARCHAR(255),
    slug VARCHAR(255) NOT NULL UNIQUE,
    content TEXT,
    thumbnail VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    published_at DATETIME(6),
    visibility VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    scheduled_publish_at DATETIME(6),
    view_count BIGINT NOT NULL DEFAULT 0,
    view BIGINT NOT NULL DEFAULT 0,
    is_publish BOOLEAN NOT NULL DEFAULT FALSE,
    user_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_post_author FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Many-to-Many: Post & Category
CREATE TABLE IF NOT EXISTS post_category (
    post_id VARCHAR(36) NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (post_id, category_id),
    CONSTRAINT fk_post_cat_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
    CONSTRAINT fk_post_cat_cat FOREIGN KEY (category_id) REFERENCES category(category_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Many-to-Many: Post & Tags
CREATE TABLE IF NOT EXISTS post_tags (
    post_id VARCHAR(36) NOT NULL,
    tag_id BINARY(16) NOT NULL,
    PRIMARY KEY (post_id, tag_id),
    CONSTRAINT fk_post_tags_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
    CONSTRAINT fk_post_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(uuid) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
