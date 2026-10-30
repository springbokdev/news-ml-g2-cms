-- Every version of a news item is a row of its own; a GUID is no longer unique, a (GUID, version) pair is.
ALTER TABLE article_content DROP CONSTRAINT uq_article_content_guid;
ALTER TABLE article_content ADD CONSTRAINT uq_article_content_guid_version UNIQUE (guid, version);
