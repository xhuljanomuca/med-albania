CREATE TABLE course_grant_audit
(
    id        UUID PRIMARY KEY,
    grant_id  UUID        NOT NULL,
    user_id   UUID        NOT NULL,
    course_id UUID        NOT NULL,
    action    VARCHAR(16) NOT NULL CHECK (action IN ('GRANTED','REVOKED')
) ,
 actor_id UUID NOT NULL,
 occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_grant_audit_user_time ON course_grant_audit (user_id, occurred_at DESC);
CREATE INDEX idx_grant_audit_course_time ON course_grant_audit (course_id, occurred_at DESC);
