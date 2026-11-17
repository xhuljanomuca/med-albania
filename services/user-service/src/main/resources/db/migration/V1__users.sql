CREATE TABLE member_profile (
 id UUID PRIMARY KEY, keycloak_id UUID UNIQUE NOT NULL, academy VARCHAR(24) NOT NULL,
 category VARCHAR(24) NOT NULL, first_name VARCHAR(120) NOT NULL, father_name VARCHAR(120),
 surname VARCHAR(120) NOT NULL, national_id VARCHAR(60), email VARCHAR(254) UNIQUE NOT NULL,
 phone VARCHAR(50), city VARCHAR(120), workplace VARCHAR(255), profession VARCHAR(120),
 university VARCHAR(255), degree VARCHAR(80), discipline VARCHAR(120), study_year INTEGER,
 locale VARCHAR(2) NOT NULL DEFAULT 'sq', status VARCHAR(24) NOT NULL,
 payment_file VARCHAR(255), payment_original_name VARCHAR(255), rejection_reason TEXT,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_profile_status ON member_profile(status);
