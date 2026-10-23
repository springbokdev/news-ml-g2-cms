package space.springbok.example.newsitem.adapter.in.web;

/**
 * Web-facing request body for creating a news item.
 */
record CreateNewsItemRequest(String headline, String body, String language) {

}
