-- Learner-scoped notifications, no cross-service foreign keys (each service owns its database).
CREATE TABLE learner_notification
(
    id         UUID PRIMARY KEY,
    user_id    UUID         NOT NULL,
    category   VARCHAR(40)  NOT NULL CHECK (category IN ('COURSE_ACCESS', 'COURSE_REVOKED', 'GENERAL')),
    title_en   VARCHAR(240) NOT NULL,
    title_sq   VARCHAR(240) NOT NULL,
    body_en    TEXT         NOT NULL,
    body_sq    TEXT         NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    read_at    TIMESTAMPTZ,
    CONSTRAINT learner_notification_read_after_created CHECK (read_at IS NULL OR read_at >= created_at)
);
CREATE INDEX idx_learner_notification_user_recent ON learner_notification (user_id, created_at DESC, id DESC);
CREATE INDEX idx_learner_notification_unread ON learner_notification (user_id, created_at DESC) WHERE read_at IS NULL;
