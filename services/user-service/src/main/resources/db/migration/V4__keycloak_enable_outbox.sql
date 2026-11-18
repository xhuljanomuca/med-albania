-- Approval is committed locally before attempting remote Keycloak enablement.
CREATE TABLE keycloak_enable_outbox (
 id UUID PRIMARY KEY,
 profile_id UUID NOT NULL REFERENCES member_profile(id),
 keycloak_id UUID NOT NULL,
 retry_count INTEGER NOT NULL DEFAULT 0 CHECK (retry_count >= 0),
 next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 last_error TEXT,
 claimed_until TIMESTAMPTZ,
 claim_token UUID,
 completed_at TIMESTAMPTZ,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 CONSTRAINT uq_keycloak_enable_profile UNIQUE (profile_id)
);
CREATE INDEX idx_keycloak_enable_due ON keycloak_enable_outbox(next_attempt_at,id) WHERE completed_at IS NULL;
