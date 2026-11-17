CREATE TABLE course_grant (id UUID PRIMARY KEY, user_id UUID NOT NULL, course_id UUID NOT NULL, granted_at TIMESTAMPTZ NOT NULL DEFAULT now(), UNIQUE(user_id,course_id));
