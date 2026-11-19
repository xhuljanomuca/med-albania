ALTER TABLE progress_outbox
    ADD COLUMN next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE progress_outbox
    ADD COLUMN claimed_until TIMESTAMPTZ;
ALTER TABLE progress_outbox
    ADD COLUMN dead_lettered_at TIMESTAMPTZ;
ALTER TABLE progress_outbox
    ADD COLUMN last_error VARCHAR(128);
CREATE INDEX idx_progress_outbox_due ON progress_outbox (next_attempt_at, id) WHERE delivered_at IS NULL AND dead_lettered_at IS NULL;
