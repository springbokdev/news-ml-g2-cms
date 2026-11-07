-- Every version records the name of the author who created it.
ALTER TABLE article_content ADD COLUMN author VARCHAR(255);
-- Rows stored before V8 have no recorded author; there is no better value to backfill.
UPDATE article_content SET author = 'unknown' WHERE author IS NULL;
ALTER TABLE article_content ALTER COLUMN author SET NOT NULL;
