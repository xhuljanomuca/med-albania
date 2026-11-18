CREATE TABLE progress_outbox (
 id UUID PRIMARY KEY,
 attempt_id UUID NOT NULL UNIQUE REFERENCES attempt(id),
 chapter_id UUID NOT NULL,
 user_id UUID NOT NULL,
 created_at TIMESTAMPTZ NOT NULL,
 delivered_at TIMESTAMPTZ,
 retry_count INTEGER NOT NULL DEFAULT 0 CHECK (retry_count >= 0)
);
CREATE INDEX idx_progress_outbox_pending ON progress_outbox(created_at) WHERE delivered_at IS NULL;
