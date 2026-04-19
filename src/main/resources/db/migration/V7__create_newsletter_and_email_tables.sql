-- ============================================================================
-- V7: Newsletter and Email Tables
-- ============================================================================
-- Entities: newsletter_campaigns, email_logs
-- Dependencies: user (V2), newsletter_subscribers (V1)
-- Note: Uses string IDs (UUID) for campaign and subscriber references
-- ============================================================================

-- -----------------------------------------------------
-- Table 'newsletter_campaigns'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `newsletter_campaigns` (
  `id` VARCHAR(36) NOT NULL,
  `name` VARCHAR(200) NOT NULL,
  `subject` VARCHAR(500) NOT NULL,
  `html_content` LONGTEXT NOT NULL,
  `text_content` TEXT NULL,
  `from_name` VARCHAR(100) NULL,
  `from_email` VARCHAR(255) NULL,
  `reply_to` VARCHAR(255) NULL,
  `status` ENUM('DRAFT', 'SCHEDULED', 'SENDING', 'SENT', 'PAUSED', 'CANCELLED') NOT NULL DEFAULT 'DRAFT',
  `scheduled_at` TIMESTAMP NULL DEFAULT NULL,
  `sent_at` TIMESTAMP NULL DEFAULT NULL,
  `target_segment` VARCHAR(500) NULL,
  `target_tags` VARCHAR(500) NULL,
  `recipient_count` BIGINT NULL,
  `sent_count` BIGINT NOT NULL DEFAULT 0,
  `opened_count` BIGINT NOT NULL DEFAULT 0,
  `clicked_count` BIGINT NOT NULL DEFAULT 0,
  `bounced_count` BIGINT NOT NULL DEFAULT 0,
  `unsubscribed_count` BIGINT NOT NULL DEFAULT 0,
  `complained_count` BIGINT NOT NULL DEFAULT 0,
  `batch_size` INT NOT NULL DEFAULT 100,
  `send_interval_seconds` INT NOT NULL DEFAULT 1,
  `created_by` VARCHAR(36) NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `utm_source` VARCHAR(100) NULL,
  `utm_medium` VARCHAR(100) NULL,
  `utm_campaign` VARCHAR(100) NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_campaign_status` (`status` ASC),
  INDEX `idx_campaign_scheduled_at` (`scheduled_at` ASC),
  INDEX `idx_campaign_created_at` (`created_at` DESC),
  INDEX `idx_campaign_created_by` (`created_by` ASC),
  CONSTRAINT `fk_campaign_creator`
    FOREIGN KEY (`created_by`)
    REFERENCES `user` (`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Email newsletter campaigns with tracking metrics and segmentation';

-- -----------------------------------------------------
-- Table 'email_logs'
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `email_logs` (
  `id` VARCHAR(36) NOT NULL,
  `campaign_id` VARCHAR(36) NULL,
  `subscriber_id` VARCHAR(36) NOT NULL,
  `recipient_email` VARCHAR(255) NOT NULL,
  `subject` VARCHAR(500) NULL,
  `status` ENUM('PENDING', 'QUEUED', 'SENDING', 'SENT', 'DELIVERED', 'OPENED', 'CLICKED', 'BOUNCED', 'COMPLAINED', 'FAILED', 'RETRYING') NOT NULL DEFAULT 'PENDING',
  `external_message_id` VARCHAR(200) NULL,
  `error_message` VARCHAR(1000) NULL,
  `opened_at` TIMESTAMP NULL DEFAULT NULL,
  `clicked_at` TIMESTAMP NULL DEFAULT NULL,
  `ip_address` VARCHAR(45) NULL,
  `user_agent` VARCHAR(500) NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `sent_at` TIMESTAMP NULL DEFAULT NULL,
  `bounce_reason` VARCHAR(500) NULL,
  `bounce_type` VARCHAR(50) NULL,
  `complaint_type` VARCHAR(50) NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_email_campaign` (`campaign_id` ASC),
  INDEX `idx_email_subscriber` (`subscriber_id` ASC),
  INDEX `idx_email_status` (`status` ASC),
  INDEX `idx_email_created_at` (`created_at` DESC),
  INDEX `idx_email_message_id` (`external_message_id` ASC),
  INDEX `idx_email_recipient` (`recipient_email` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Detailed email delivery log with opens, clicks, bounces, and complaints';
