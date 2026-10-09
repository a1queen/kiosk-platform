ALTER TABLE outbox_events ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT 'PENDING';
ALTER TABLE outbox_events ADD COLUMN attempts INT NOT NULL DEFAULT 0;

CREATE INDEX idx_outbox_status_created_at ON outbox_events (status, created_at ASC);
