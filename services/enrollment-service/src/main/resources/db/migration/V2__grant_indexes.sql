CREATE INDEX IF NOT EXISTS idx_course_grant_course ON course_grant(course_id);
CREATE INDEX IF NOT EXISTS idx_course_grant_granted_at ON course_grant(granted_at DESC);
