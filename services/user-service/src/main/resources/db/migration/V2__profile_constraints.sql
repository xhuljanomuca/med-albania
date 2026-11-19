-- Preserve existing records; constrain future profile data without assuming old values.
CREATE INDEX IF NOT EXISTS idx_member_profile_academy_category ON member_profile(academy,category);
CREATE INDEX IF NOT EXISTS idx_member_profile_created_at ON member_profile(created_at DESC);
ALTER TABLE member_profile
    ADD CONSTRAINT member_profile_locale_ck CHECK (locale IN ('sq', 'en')) NOT VALID;
