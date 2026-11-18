CREATE INDEX IF NOT EXISTS idx_course_instructor_status ON course(instructor_id,status) WHERE instructor_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_chapter_progress_chapter_user ON chapter_progress(chapter_id,user_id);
