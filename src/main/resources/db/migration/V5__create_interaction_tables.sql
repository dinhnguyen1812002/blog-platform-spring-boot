-- ============================================================================
-- V5: Interaction Tables
-- ============================================================================
-- Entities: comment, rating, bookmark
-- Dependencies: user (V2), post (V3)
-- ============================================================================

-- -----------------------------------------------------
-- Table 'comment'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `comment` (
  `id` VARCHAR(36) NOT NULL,
  `content` LONGTEXT NOT NULL,
  `post_id` VARCHAR(36) NOT NULL,
  `user_id` VARCHAR(36) NOT NULL,
  `parent_id` VARCHAR(36) NULL,
  `depth` INT NOT NULL DEFAULT 0,
  `reply_count` INT NOT NULL DEFAULT 0,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_comment_post`
    FOREIGN KEY (`post_id`)
    REFERENCES `post` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_comment_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_comment_parent`
    FOREIGN KEY (`parent_id`)
    REFERENCES `comment` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  INDEX `idx_comment_post` (`post_id` ASC),
  INDEX `idx_comment_user` (`user_id` ASC),
  INDEX `idx_comment_parent` (`parent_id` ASC),
  INDEX `idx_comment_created` (`created_at` ASC),
  INDEX `idx_comment_depth` (`depth` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User comments on posts with nested/threaded replies support';

-- -----------------------------------------------------
-- Table 'rating'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `rating` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `score` INT NOT NULL,
  `created_at` TIMESTAMP NOT NULL,
  `post_id` VARCHAR(36) NOT NULL,
  `user_id` VARCHAR(36) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_rating_post`
    FOREIGN KEY (`post_id`)
    REFERENCES `post` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_rating_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  UNIQUE INDEX `uk_user_post_rating` (`user_id` ASC, `post_id` ASC),
  INDEX `idx_rating_post` (`post_id` ASC),
  INDEX `idx_rating_user` (`user_id` ASC),
  INDEX `idx_rating_score` (`score` ASC),
  CHECK (`score` >= 1 AND `score` <= 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User ratings/star reviews for posts (1-5 scale)';

-- -----------------------------------------------------
-- Table 'bookmark'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `bookmark` (
  `id` VARCHAR(36) NOT NULL,
  `user_id` VARCHAR(36) NOT NULL,
  `post_id` VARCHAR(36) NOT NULL,
  `saved_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `notes` TEXT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_bookmark_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_bookmark_post`
    FOREIGN KEY (`post_id`)
    REFERENCES `post` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  UNIQUE INDEX `uk_user_post_bookmark` (`user_id` ASC, `post_id` ASC),
  INDEX `idx_bookmark_user` (`user_id` ASC),
  INDEX `idx_bookmark_post` (`post_id` ASC),
  INDEX `idx_bookmark_saved` (`saved_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User bookmarked/saved posts with optional notes';
