CREATE TABLE certificate (
 id uuid PRIMARY KEY,
 user_id uuid NOT NULL,
 course_id uuid NOT NULL,
 exam_attempt_id uuid NOT NULL UNIQUE,
 verification_code varchar(80) NOT NULL UNIQUE,
 recipient_name varchar(255) NOT NULL,
 course_title_sq varchar(255) NOT NULL,
 course_title_en varchar(255) NOT NULL,
 score_percent integer NOT NULL CHECK (score_percent BETWEEN 0 AND 100),
 issued_at timestamptz NOT NULL,
 expires_at timestamptz,
 revoked_at timestamptz,
 status varchar(16) NOT NULL CHECK (status IN ('ISSUED','REVOKED')),
 accredited boolean NOT NULL DEFAULT false,
 credit_hours numeric(7,2),
 accrediting_body varchar(255),
 CONSTRAINT credits_only_if_accredited CHECK (accredited OR (credit_hours IS NULL AND accrediting_body IS NULL))
);
CREATE INDEX idx_certificate_user ON certificate(user_id);
CREATE INDEX idx_certificate_course ON certificate(course_id);
CREATE TABLE achievement(id uuid PRIMARY KEY,user_id uuid NOT NULL,course_id uuid NOT NULL,certificate_id uuid NOT NULL REFERENCES certificate(id),label_sq varchar(255) NOT NULL,label_en varchar(255) NOT NULL,awarded_at timestamptz NOT NULL);
