package space.springbok.example.newsitem.adapter.in.web;

/**
 * Web-facing response body for a newly created news item.
 */
record CreateNewsItemResponse(String guid, int version) {

}
