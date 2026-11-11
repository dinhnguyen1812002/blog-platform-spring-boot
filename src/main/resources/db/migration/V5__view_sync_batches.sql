-- A batch marker and its view increment are committed in the same database transaction.
CREATE TABLE IF NOT EXISTS view_sync_batch (
    batch_id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
