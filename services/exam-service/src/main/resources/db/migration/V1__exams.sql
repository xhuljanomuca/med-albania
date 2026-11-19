CREATE TABLE quiz
(
    id                 UUID PRIMARY KEY,
    course_id          UUID         NOT NULL,
    chapter_id         UUID,
    title_sq           VARCHAR(255) NOT NULL,
    title_en           VARCHAR(255) NOT NULL,
    status             VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    passing_percent    INTEGER      NOT NULL DEFAULT 70 CHECK (passing_percent BETWEEN 1 AND 100),
    time_limit_minutes INTEGER      NOT NULL DEFAULT 30 CHECK (time_limit_minutes BETWEEN 1 AND 240),
    max_attempts       INTEGER      NOT NULL DEFAULT 3 CHECK (max_attempts BETWEEN 1 AND 100),
    randomize          BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT quiz_status CHECK (status IN ('DRAFT', 'PUBLISHED')),
    CONSTRAINT quiz_scope CHECK (chapter_id IS NULL OR chapter_id <> '00000000-0000-0000-0000-000000000000')
);
CREATE UNIQUE INDEX uq_quiz_chapter ON quiz (chapter_id) WHERE chapter_id IS NOT NULL;
CREATE UNIQUE INDEX uq_quiz_final ON quiz (course_id) WHERE chapter_id IS NULL;
CREATE TABLE question
(
    id             UUID PRIMARY KEY,
    quiz_id        UUID    NOT NULL REFERENCES quiz (id),
    position       INTEGER NOT NULL CHECK (position > 0),
    prompt_sq      TEXT    NOT NULL,
    prompt_en      TEXT    NOT NULL,
    choices_sq     TEXT    NOT NULL,
    choices_en     TEXT    NOT NULL,
    correct_index  INTEGER NOT NULL CHECK (correct_index BETWEEN 0 AND 3),
    explanation_sq TEXT,
    explanation_en TEXT,
    UNIQUE (quiz_id, position)
);
CREATE TABLE attempt
(
    id             UUID PRIMARY KEY,
    quiz_id        UUID        NOT NULL REFERENCES quiz (id),
    user_id        UUID        NOT NULL,
    attempt_number INTEGER     NOT NULL,
    started_at     TIMESTAMPTZ NOT NULL,
    submitted_at   TIMESTAMPTZ,
    score_percent  INTEGER,
    passed         BOOLEAN,
    question_order TEXT        NOT NULL,
    UNIQUE (quiz_id, user_id, attempt_number)
);
CREATE TABLE answer
(
    attempt_id     UUID    NOT NULL REFERENCES attempt (id),
    question_id    UUID    NOT NULL REFERENCES question (id),
    selected_index INTEGER NOT NULL CHECK (selected_index BETWEEN 0 AND 3),
    correct        BOOLEAN NOT NULL,
    PRIMARY KEY (attempt_id, question_id)
);
CREATE INDEX idx_attempt_user ON attempt (user_id, quiz_id);
