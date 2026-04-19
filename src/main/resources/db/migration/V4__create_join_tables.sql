-- ============================================================================
-- V4: Join Tables (Many-to-Many Relationships)
-- ============================================================================
-- Join Tables: user_roles, post_like, post_category, post_tags
-- Dependencies: user (V2), roles (V1), post (V3), category (V1), tags (V1)
-- ============================================================================

-- -----------------------------------------------------
-- Table 'user_roles' (User <-> Role)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_roles` (
  `user_id` VARCHAR(36) NOT NULL,
  `role_id` INT NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`),
  CONSTRAINT `fk_userroles_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_userroles_role`
    FOREIGN KEY (`role_id`)
    REFERENCES `roles` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Many-to-many join table linking users to their roles/authorities';

-- -----------------------------------------------------
-- Table 'post_like' (Post <-> User likes)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `post_like` (
  `post_id` VARCHAR(36) NOT NULL,
  `user_id` VARCHAR(36) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`post_id`, `user_id`),
  CONSTRAINT `fk_postlike_post`
    FOREIGN KEY (`post_id`)
    REFERENCES `post` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_postlike_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  INDEX `idx_postlike_user` (`user_id` ASC),
  INDEX `idx_postlike_created` (`created_at` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Many-to-many join table tracking post likes by users';

-- -----------------------------------------------------
-- Table 'post_category' (Post <-> Category)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `post_category` (
  `post_id` VARCHAR(36) NOT NULL,
  `category_id` BIGINT NOT NULL,
  PRIMARY KEY (`post_id`, `category_id`),
  CONSTRAINT `fk_postcategory_post`
    FOREIGN KEY (`post_id`)
    REFERENCES `post` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_postcategory_category`
    FOREIGN KEY (`category_id`)
    REFERENCES `category` (`category_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  INDEX `idx_postcategory_category` (`category_id` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Many-to-many join table linking posts to categories';

-- -----------------------------------------------------
-- Table 'post_tags' (Post <-> Tags)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `post_tags` (
  `post_id` VARCHAR(36) NOT NULL,
  `tag_id` VARCHAR(36) NOT NULL,
  PRIMARY KEY (`post_id`, `tag_id`),
  CONSTRAINT `fk_posttags_post`
    FOREIGN KEY (`post_id`)
    REFERENCES `post` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_posttags_tag`
    FOREIGN KEY (`tag_id`)
    REFERENCES `tags` (`uuid`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  INDEX `idx_posttags_tag` (`tag_id` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Many-to-many join table linking posts to tags';
