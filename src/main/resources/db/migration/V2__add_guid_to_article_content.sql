ALTER TABLE article_content ADD COLUMN guid VARCHAR(255);
ALTER TABLE article_content ADD CONSTRAINT uq_article_content_guid UNIQUE (guid);
