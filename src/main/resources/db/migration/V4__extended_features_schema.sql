-- Migration: V4__extended_features_schema.sql
-- Purpose: Create tables for Series and Newsletters

-- 1. Series table
CREATE TABLE IF NOT EXISTS series (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(250) NOT NULL UNIQUE,
    description TEXT,
    thumbnail VARCHAR(255),
    user_id VARCHAR(36) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    total_posts INT DEFAULT 0,
    view_count BIGINT DEFAULT 0,
    CONSTRAINT fk_series_user FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Series Post (Join table with metadata)
CREATE TABLE IF NOT EXISTS series_post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    series_id VARCHAR(36) NOT NULL,
    post_id VARCHAR(36) NOT NULL,
    order_index INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_sp_series FOREIGN KEY (series_id) REFERENCES series(id) ON DELETE CASCADE,
    CONSTRAINT fk_sp_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
    UNIQUE KEY uk_series_post (series_id, post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Newsletter subscription table
CREATE TABLE IF NOT EXISTS newsletter_subscription (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    frequency VARCHAR(20) NOT NULL DEFAULT 'DAILY',
    subscription_token VARCHAR(255) UNIQUE,
    confirmation_token VARCHAR(255),
    subscribed_at DATETIME(6),
    confirmed_at DATETIME(6),
    unsubscribed_at DATETIME(6),
    last_sent_at DATETIME(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
