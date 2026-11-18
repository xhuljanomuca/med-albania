CREATE TABLE payment_decision_audit (
 id UUID PRIMARY KEY,
 profile_id UUID NOT NULL REFERENCES member_profile(id),
 actor_id UUID NOT NULL,
 decision VARCHAR(16) NOT NULL CHECK (decision IN ('APPROVED','REJECTED')),
 reason TEXT,
 occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_payment_decision_profile_time ON payment_decision_audit(profile_id,occurred_at DESC);
