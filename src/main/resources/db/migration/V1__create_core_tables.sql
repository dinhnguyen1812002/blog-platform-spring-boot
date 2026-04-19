-- ============================================================================
-- V1: Standalone Tables (No Foreign Key Dependencies)
-- ============================================================================
-- Entities: roles, category, tags, meme, traffic_counters,
--           notification_templates, newsletter_subscription, newsletter_subscribers
-- ============================================================================

-- -----------------------------------------------------
-- Table 'roles'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `roles` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_role_name` (`name` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User roles/authorities (e.g., ROLE_USER, ROLE_ADMIN)';

-- Insert default roles
INSERT INTO `roles` (`name`) VALUES ('ROLE_USER'), ('ROLE_ADMIN'), ('ROLE_MODERATOR') ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- -----------------------------------------------------
-- Table 'category'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `category` (
  `category_id` BIGINT NOT NULL AUTO_INCREMENT,
  `category` VARCHAR(100) NOT NULL,
  `slug` VARCHAR(100) NOT NULL,
  `background_color` VARCHAR(7) NULL,
  `description` VARCHAR(500) NULL,
  PRIMARY KEY (`category_id`),
  UNIQUE INDEX `uk_category_name` (`category` ASC),
  UNIQUE INDEX `uk_category_slug` (`slug` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Blog post categories for content organization';

-- -----------------------------------------------------
-- Table 'tags'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tags` (
  `uuid` VARCHAR(36) NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `slug` VARCHAR(100) NULL,
  `description` TEXT NULL,
  `color` VARCHAR(20) NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`uuid`),
  UNIQUE INDEX `uk_tags_name` (`name` ASC),
  INDEX `idx_tags_slug` (`slug` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tags for categorizing posts by topic';

-- -----------------------------------------------------
-- Table 'meme'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `meme` (
  `id` VARCHAR(36) NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `description` TEXT NULL,
  `meme_url` VARCHAR(500) NOT NULL,
  `slug` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_meme_slug` (`slug` ASC),
  INDEX `idx_meme_name` (`name` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Meme templates for content creation';

-- -----------------------------------------------------
-- Table 'traffic_counters'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `traffic_counters` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `period_date` DATE NOT NULL,
  `period_type` ENUM('DAY', 'MONTH', 'YEAR') NOT NULL,
  `access_count` BIGINT NOT NULL DEFAULT 0,
  `zone_id` VARCHAR(100) NOT NULL DEFAULT 'UTC',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_traffic_period_date` (`period_type` ASC, `period_date` ASC),
  INDEX `idx_traffic_period_type` (`period_type` ASC),
  INDEX `idx_traffic_period_date` (`period_date` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Website traffic analytics aggregated by period (day/month/year)';

-- -----------------------------------------------------
-- Table 'notification_templates'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `notification_templates` (
  `id` VARCHAR(36) NOT NULL,
  `code` VARCHAR(50) NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `type` VARCHAR(50) NOT NULL,
  `subject` VARCHAR(200) NULL,
  `title` VARCHAR(200) NOT NULL,
  `content` TEXT NOT NULL,
  `email_html_template` LONGTEXT NULL,
  `email_text_template` TEXT NULL,
  `push_title` VARCHAR(100) NULL,
  `push_body` TEXT NULL,
  `action_url` VARCHAR(500) NULL,
  `action_text` VARCHAR(50) NULL,
  `icon` VARCHAR(50) NULL,
  `color` VARCHAR(20) NULL,
  `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
  `version` INT NOT NULL DEFAULT 1,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `created_by` VARCHAR(36) NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_template_code` (`code` ASC),
  INDEX `idx_template_type` (`type` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Reusable notification message templates with multi-channel support';

-- -----------------------------------------------------
-- Table 'newsletter_subscription'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `newsletter_subscription` (
  `id` VARCHAR(36) NOT NULL,
  `email` VARCHAR(255) NOT NULL,
  `name` VARCHAR(100) NULL,
  `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
  `is_confirmed` BOOLEAN NOT NULL DEFAULT FALSE,
  `frequency` VARCHAR(20) NOT NULL DEFAULT 'DAILY',
  `subscription_token` VARCHAR(255) UNIQUE NULL,
  `confirmation_token` VARCHAR(36) UNIQUE NULL,
  `subscribed_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `confirmed_at` TIMESTAMP NULL DEFAULT NULL,
  `unsubscribed_at` TIMESTAMP NULL DEFAULT NULL,
  `last_sent_at` TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_newsletter_email` (`email` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Main newsletter subscription list for blog updates';

-- -----------------------------------------------------
-- Table 'newsletter_subscribers'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `newsletter_subscribers` (
  `id` VARCHAR(36) NOT NULL,
  `email` VARCHAR(255) NOT NULL,
  `first_name` VARCHAR(100) NULL,
  `last_name` VARCHAR(100) NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  `source_url` VARCHAR(500) NULL,
  `signup_ip` VARCHAR(45) NULL,
  `confirmed_ip` VARCHAR(45) NULL,
  `unsubscribe_token` VARCHAR(36) NOT NULL,
  `confirmation_token` VARCHAR(36) UNIQUE NULL,
  `confirmation_token_expires_at` TIMESTAMP NULL DEFAULT NULL,
  `preferences` TEXT NULL,
  `tags` VARCHAR(500) NULL,
  `metadata` TEXT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `confirmed_at` TIMESTAMP NULL DEFAULT NULL,
  `unsubscribed_at` TIMESTAMP NULL DEFAULT NULL,
  `last_sent_at` TIMESTAMP NULL DEFAULT NULL,
  `bounce_count` INT NOT NULL DEFAULT 0,
  `last_bounce_at` TIMESTAMP NULL DEFAULT NULL,
  `gdpr_consent` BOOLEAN NOT NULL DEFAULT FALSE,
  `gdpr_consent_at` TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_newsletter_email_unique` (`email` ASC),
  UNIQUE INDEX `idx_newsletter_token` (`unsubscribe_token` ASC),
  UNIQUE INDEX `idx_newsletter_confirm_token` (`confirmation_token` ASC),
  INDEX `idx_newsletter_status` (`status` ASC),
  INDEX `idx_newsletter_created_at` (`created_at` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Detailed newsletter subscriber management with GDPR compliance';
