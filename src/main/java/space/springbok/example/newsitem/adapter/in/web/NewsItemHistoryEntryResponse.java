package space.springbok.example.newsitem.adapter.in.web;

import java.time.OffsetDateTime;

/**
 * Web-facing entry in the modification history of a news item: one stored version.
 */
record NewsItemHistoryEntryResponse(int version, String author, OffsetDateTime versionCreated) {

}
