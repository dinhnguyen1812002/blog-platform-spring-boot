-- ============================================================================
-- V6: Notification Tables
-- ============================================================================
-- Entities: notifications, user_notification_preferences, notification_history
-- Dependencies: user (V2)
-- Note: notification_templates created in V1
-- ============================================================================

-- -----------------------------------------------------
-- Table 'notifications'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `notifications` (
  `notification_id` VARCHAR(50) NOT NULL,
  `user_id` VARCHAR(36) NOT NULL,
  `type` VARCHAR(50) NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `message` TEXT NOT NULL,
  `is_read` BOOLEAN NOT NULL DEFAULT FALSE,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notification_id`),
  CONSTRAINT `fk_notifications_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  INDEX `idx_notifications_user` (`user_id` ASC),
  INDEX `idx_notifications_read` (`is_read` ASC),
  INDEX `idx_notifications_created` (`created_at` DESC),
  INDEX `idx_notifications_type` (`type` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User notification inbox with read status tracking';

-- -----------------------------------------------------
-- Table 'user_notification_preferences'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_notification_preferences` (
  `id` VARCHAR(36) NOT NULL,
  `user_id` VARCHAR(36) NOT NULL,
  `channel` ENUM('EMAIL', 'PUSH', 'SMS', 'IN_APP') NOT NULL,
  `enabled` BOOLEAN NOT NULL DEFAULT TRUE,
  `digest_mode` VARCHAR(20) NOT NULL DEFAULT 'immediate',
  `quiet_hours_start` INT NULL,
  `quiet_hours_end` INT NULL,
  `email_address` VARCHAR(255) NULL,
  `push_token` VARCHAR(500) NULL,
  `device_type` VARCHAR(20) NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_prefs_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  UNIQUE INDEX `idx_prefs_user_channel` (`user_id` ASC, `channel` ASC),
  INDEX `idx_prefs_user_id` (`user_id` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User notification channel preferences and settings';

-- -----------------------------------------------------
-- Table 'notification_history'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `notification_history` (
  `id` VARCHAR(36) NOT NULL,
  `notification_id` VARCHAR(36) NOT NULL,
  `user_id` VARCHAR(36) NOT NULL,
  `channel` ENUM('EMAIL', 'PUSH', 'SMS', 'IN_APP') NOT NULL,
  `status` ENUM('PENDING', 'SENT', 'DELIVERED', 'FAILED', 'READ') NOT NULL DEFAULT 'PENDING',
  `recipient_address` VARCHAR(255) NULL,
  `subject` VARCHAR(500) NULL,
  `content` LONGTEXT NULL,
  `external_message_id` VARCHAR(200) NULL,
  `error_message` VARCHAR(1000) NULL,
  `retry_count` INT NOT NULL DEFAULT 0,
  `max_retries` INT NOT NULL DEFAULT 3,
  `next_retry_at` TIMESTAMP NULL DEFAULT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `sent_at` TIMESTAMP NULL DEFAULT NULL,
  `delivered_at` TIMESTAMP NULL DEFAULT NULL,
  `read_at` TIMESTAMP NULL DEFAULT NULL,
  `updated_at` TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_history_user_id` (`user_id` ASC),
  INDEX `idx_history_status` (`status` ASC),
  INDEX `idx_history_channel` (`channel` ASC),
  INDEX `idx_history_created_at` (`created_at` ASC),
  INDEX `idx_history_notification_id` (`notification_id` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Audit log for all notification delivery attempts and outcomes';
