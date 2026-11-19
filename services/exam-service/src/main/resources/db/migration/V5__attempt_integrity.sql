-- A learner may have multiple sequential attempts, but never the same numbered attempt twice.
CREATE INDEX IF NOT EXISTS idx_attempt_quiz_user_started ON attempt(quiz_id,user_id,started_at DESC);
CREATE INDEX IF NOT EXISTS idx_progress_outbox_dead_letter ON progress_outbox(dead_lettered_at) WHERE dead_lettered_at IS NOT NULL;
ALTER TABLE attempt
    ADD CONSTRAINT attempt_number_positive_ck CHECK (attempt_number > 0) NOT VALID;
ALTER TABLE attempt
    ADD CONSTRAINT attempt_score_range_ck CHECK (score_percent IS NULL OR score_percent BETWEEN 0 AND 100) NOT VALID;
