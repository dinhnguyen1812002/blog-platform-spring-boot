-- ============================================================================
-- V3: Post, Series, and SeriesPost Tables
-- ============================================================================
-- Entities: post, series, series_post
-- Dependencies: user (V2)
-- ============================================================================

-- -----------------------------------------------------
-- Table 'post'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `post` (
  `id` VARCHAR(36) NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `excerpt` VARCHAR(1000) NULL,
  `slug` VARCHAR(255) NOT NULL,
  `content` LONGTEXT NOT NULL,
  `thumbnail` VARCHAR(500) NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `featured` BOOLEAN NOT NULL DEFAULT FALSE,
  `published_at` TIMESTAMP NULL DEFAULT NULL,
  `visibility` ENUM('DRAFT', 'PUBLISHED', 'PRIVATE', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT',
  `scheduled_publish_at` TIMESTAMP NULL DEFAULT NULL,
  `view_count` BIGINT NOT NULL DEFAULT 0,
  `view` BIGINT NOT NULL DEFAULT 0,
  `is_publish` BOOLEAN NOT NULL DEFAULT FALSE,
  `user_id` VARCHAR(36) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_post_slug` (`slug` ASC),
  INDEX `idx_post_user` (`user_id` ASC),
  INDEX `idx_post_visibility` (`visibility` ASC),
  INDEX `idx_post_published_at` (`published_at` ASC),
  INDEX `idx_post_created_at` (`created_at` ASC),
  INDEX `idx_post_featured` (`featured` ASC),
  FULLTEXT INDEX `ft_post_title_content` (`title`, `content`),
  CONSTRAINT `fk_post_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Core blog post/article content with publishing workflow';

-- -----------------------------------------------------
-- Table 'series'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `series` (
  `id` VARCHAR(36) NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  `slug` VARCHAR(250) NOT NULL,
  `description` LONGTEXT NULL,
  `thumbnail` VARCHAR(500) NULL,
  `user_id` VARCHAR(36) NOT NULL,
  `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
  `is_completed` BOOLEAN NOT NULL DEFAULT FALSE,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `total_posts` INT NOT NULL DEFAULT 0,
  `view_count` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_series_slug` (`slug` ASC),
  INDEX `idx_series_user` (`user_id` ASC),
  INDEX `idx_series_active` (`is_active` ASC),
  CONSTRAINT `fk_series_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Series/collection of related blog posts with ordering';

-- -----------------------------------------------------
-- Table 'series_post'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `series_post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `series_id` VARCHAR(36) NOT NULL,
  `post_id` VARCHAR(36) NOT NULL,
  `order_index` INT NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_seriespost_series`
    FOREIGN KEY (`series_id`)
    REFERENCES `series` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_seriespost_post`
    FOREIGN KEY (`post_id`)
    REFERENCES `post` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  UNIQUE INDEX `uk_series_post` (`series_id` ASC, `post_id` ASC),
  INDEX `idx_seriespost_order` (`series_id` ASC, `order_index` ASC),
  INDEX `idx_seriespost_post` (`post_id` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Join table linking posts to series with ordering information';
