-- Bounded retries and operator-controlled recovery for remote identity synchronization.
ALTER TABLE keycloak_enable_outbox ADD COLUMN dead_lettered_at TIMESTAMPTZ;
CREATE INDEX idx_keycloak_enable_dead_letter ON keycloak_enable_outbox(dead_lettered_at,id) WHERE dead_lettered_at IS NOT NULL;
DROP INDEX IF EXISTS idx_keycloak_enable_due;
CREATE INDEX idx_keycloak_enable_due ON keycloak_enable_outbox(next_attempt_at,id) WHERE completed_at IS NULL AND dead_lettered_at IS NULL;
