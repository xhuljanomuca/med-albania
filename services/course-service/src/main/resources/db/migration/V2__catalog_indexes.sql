CREATE INDEX IF NOT EXISTS idx_course_published_category ON course(category,academy) WHERE status='PUBLISHED';
CREATE INDEX IF NOT EXISTS idx_chapter_progress_chapter ON chapter_progress(chapter_id);
ALTER TABLE chapter_resource
    ADD CONSTRAINT chapter_resource_byte_size_ck CHECK (byte_size >= 0) NOT VALID;
