-- Rows stored before V5 have no recorded creation time; the migration time is the best available value.
UPDATE article_content SET version_created = CURRENT_TIMESTAMP, version_created_offset = 0 WHERE version_created IS NULL;
ALTER TABLE article_content ALTER COLUMN version_created SET NOT NULL;
ALTER TABLE article_content ALTER COLUMN version_created_offset SET NOT NULL;
