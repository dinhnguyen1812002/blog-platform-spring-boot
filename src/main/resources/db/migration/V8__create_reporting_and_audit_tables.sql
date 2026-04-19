-- ============================================================================
-- V8: Reporting, Analytics, and OAuth Audit Tables
-- ============================================================================
-- Entities: article_reports, oauth_audit_logs, video
-- Dependencies: user (V2), post (V3), oauth_accounts (V2)
-- ============================================================================

-- -----------------------------------------------------
-- Table 'video'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `video` (
  `id` VARCHAR(36) NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `file_path` VARCHAR(500) NOT NULL,
  `content_type` VARCHAR(100) NOT NULL,
  `url` VARCHAR(500) NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_video_title` (`title` ASC),
  INDEX `idx_video_content_type` (`content_type` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Video content metadata and storage references';

-- -----------------------------------------------------
-- Table 'article_reports'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `article_reports` (
  `id` VARCHAR(36) NOT NULL,
  `post_id` VARCHAR(36) NOT NULL,
  `reporter_id` VARCHAR(36) NOT NULL,
  `category` VARCHAR(50) NOT NULL,
  `description` LONGTEXT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  `admin_notes` LONGTEXT NULL,
  `reviewed_by` VARCHAR(36) NULL,
  `reviewed_at` TIMESTAMP NULL DEFAULT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_articlereport_post`
    FOREIGN KEY (`post_id`)
    REFERENCES `post` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_articlereport_reporter`
    FOREIGN KEY (`reporter_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_articlereport_reviewer`
    FOREIGN KEY (`reviewed_by`)
    REFERENCES `user` (`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  UNIQUE INDEX `uk_user_post_report` (`reporter_id` ASC, `post_id` ASC),
  INDEX `idx_report_post` (`post_id` ASC),
  INDEX `idx_report_user` (`reporter_id` ASC),
  INDEX `idx_report_status` (`status` ASC),
  INDEX `idx_report_category` (`category` ASC),
  INDEX `idx_report_created` (`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User-submitted reports for inappropriate or problematic articles with moderation workflow';

-- -----------------------------------------------------
-- Table 'oauth_audit_logs'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `oauth_audit_logs` (
  `id` VARCHAR(36) NOT NULL,
  `user_id` VARCHAR(36) NULL,
  `provider` ENUM('GOOGLE', 'GITHUB', 'DISCORD', 'FACEBOOK', 'TWITTER') NOT NULL,
  `event_type` VARCHAR(50) NOT NULL,
  `event_description` VARCHAR(500) NULL,
  `ip_address` VARCHAR(45) NULL,
  `user_agent` VARCHAR(500) NULL,
  `success` BOOLEAN NOT NULL DEFAULT TRUE,
  `failure_reason` VARCHAR(500) NULL,
  `session_id` VARCHAR(100) NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_audit_user_id` (`user_id` ASC),
  INDEX `idx_audit_event_type` (`event_type` ASC),
  INDEX `idx_audit_created_at` (`created_at` DESC),
  INDEX `idx_audit_ip_address` (`ip_address` ASC),
  INDEX `idx_audit_provider` (`provider` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Security audit log for OAuth authentication attempts and events';
