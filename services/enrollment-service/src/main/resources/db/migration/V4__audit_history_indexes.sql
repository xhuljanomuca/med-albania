-- Stable reverse-chronological learner access history.
CREATE INDEX IF NOT EXISTS idx_grant_audit_user_time_id ON course_grant_audit(user_id, occurred_at DESC, id DESC);
CREATE INDEX IF NOT EXISTS idx_grant_audit_course_action_time ON course_grant_audit(course_id, action, occurred_at DESC);
