-- ============================================================================
-- V2: User and Authentication Tables
-- ============================================================================
-- Entities: user, social_media_links, refresh_tokens, api_keys, oauth_accounts,
--           jwt_blacklist
-- Note: user_roles join table is deferred to V4 (depends on both user and roles)
-- ============================================================================

-- -----------------------------------------------------
-- Table 'user'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
  `id` VARCHAR(36) NOT NULL,
  `username` VARCHAR(50) NOT NULL,
  `slug` VARCHAR(100) UNIQUE NULL,
  `email` VARCHAR(255) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `avatar` VARCHAR(500) NULL,
  `bio` TEXT NULL,
  `website` VARCHAR(255) NULL,
  `custom_profile_markdown` LONGTEXT NULL,
  `banned` BOOLEAN NOT NULL DEFAULT FALSE,
  `ban_reason` VARCHAR(500) NULL,
  `auth_provider` VARCHAR(30) NULL,
  `provider_id` VARCHAR(100) NULL,
  `reset_token` VARCHAR(255) NULL,
  `reset_token_expiry` TIMESTAMP NULL DEFAULT NULL,
  `created_at` TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_user_username` (`username` ASC),
  UNIQUE INDEX `uk_user_email` (`email` ASC),
  INDEX `idx_user_slug` (`slug` ASC),
  INDEX `idx_user_created_at` (`created_at` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Core user account table with authentication and profile data';

-- -----------------------------------------------------
-- Table 'social_media_links'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `social_media_links` (
  `id` VARCHAR(36) NOT NULL,
  `platform` ENUM('GITHUB', 'TWITTER', 'LINKEDIN', 'FACEBOOK', 'INSTAGRAM', 'YOUTUBE', 'DISCORD', 'TELEGRAM') NOT NULL,
  `url` VARCHAR(500) NOT NULL,
  `user_id` VARCHAR(36) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_socialmedia_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  UNIQUE INDEX `uk_user_platform` (`user_id` ASC, `platform` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User social media profile links (one per platform per user)';

-- -----------------------------------------------------
-- Table 'refresh_tokens'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `refresh_tokens` (
  `id` VARCHAR(36) NOT NULL,
  `token` VARCHAR(200) NOT NULL,
  `user_id` VARCHAR(36) NOT NULL,
  `expiry_date` TIMESTAMP NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_refresh_token_token` (`token` ASC),
  CONSTRAINT `fk_refreshtoken_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  INDEX `idx_refresh_token_user` (`user_id` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Refresh tokens for JWT token rotation and revocation';

-- -----------------------------------------------------
-- Table 'api_keys'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `api_keys` (
  `id` VARCHAR(36) NOT NULL,
  `api_key` VARCHAR(255) NOT NULL,
  `api_secret_hash` VARCHAR(255) NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `active` BOOLEAN NOT NULL DEFAULT TRUE,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_used_at` TIMESTAMP NULL DEFAULT NULL,
  `user_id` VARCHAR(36) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_api_key` (`api_key` ASC),
  CONSTRAINT `fk_apikey_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  INDEX `idx_api_key_user` (`user_id` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API keys for external platform integration with secure hashing';

-- -----------------------------------------------------
-- Table 'oauth_accounts'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `oauth_accounts` (
  `id` VARCHAR(36) NOT NULL,
  `user_id` VARCHAR(36) NOT NULL,
  `provider` ENUM('GOOGLE', 'GITHUB', 'DISCORD', 'FACEBOOK', 'TWITTER') NOT NULL,
  `provider_id` VARCHAR(100) NOT NULL,
  `email` VARCHAR(255) NOT NULL,
  `provider_username` VARCHAR(100) NULL,
  `provider_avatar_url` VARCHAR(500) NULL,
  `access_token` TEXT NULL,
  `refresh_token` TEXT NULL,
  `token_expires_at` TIMESTAMP NULL DEFAULT NULL,
  `is_primary` BOOLEAN NOT NULL DEFAULT FALSE,
  `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `last_used_at` TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_oauth_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  UNIQUE INDEX `idx_oauth_provider_id` (`provider` ASC, `provider_id` ASC),
  INDEX `idx_oauth_user_id` (`user_id` ASC),
  INDEX `idx_oauth_email` (`email` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Linked OAuth provider accounts for social login';

-- -----------------------------------------------------
-- Table 'jwt_blacklist'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `jwt_blacklist` (
  `id` VARCHAR(36) NOT NULL,
  `token_hash` VARCHAR(64) NOT NULL,
  `expiry_date` TIMESTAMP NOT NULL,
  `revoked_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `reason` VARCHAR(100) NULL,
  `user_id` VARCHAR(36) NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_jwt_blacklist_token_hash` (`token_hash` ASC),
  INDEX `idx_jwt_blacklist_expiry_date` (`expiry_date` ASC),
  INDEX `idx_jwt_blacklist_user_id` (`user_id` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Revoked JWT tokens stored by SHA-256 hash for security';

-- -----------------------------------------------------
-- Table 'user_roles' (Join table - deferred to V4)
-- -----------------------------------------------------
-- Will be created in V4 after both `user` and `roles` tables exist
