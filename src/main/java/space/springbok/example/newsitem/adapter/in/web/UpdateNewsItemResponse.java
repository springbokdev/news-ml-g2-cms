package space.springbok.example.newsitem.adapter.in.web;

/**
 * Web-facing response body for an accepted news item update.
 */
record UpdateNewsItemResponse(String guid, int version) {

}
