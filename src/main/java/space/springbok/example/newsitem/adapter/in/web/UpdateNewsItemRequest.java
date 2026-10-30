package space.springbok.example.newsitem.adapter.in.web;

/**
 * Web-facing request body for updating a news item.
 */
record UpdateNewsItemRequest(String headline, String body, String language, String author) {

}
