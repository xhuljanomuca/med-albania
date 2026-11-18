-- One achievement per issued certificate, including concurrent retries.
CREATE UNIQUE INDEX IF NOT EXISTS uq_achievement_certificate ON achievement(certificate_id);
CREATE INDEX IF NOT EXISTS idx_achievement_user ON achievement(user_id,awarded_at DESC);
CREATE INDEX IF NOT EXISTS idx_certificate_issued_at ON certificate(issued_at DESC);
