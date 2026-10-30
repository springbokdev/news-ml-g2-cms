package space.springbok.example.newsitem.application.port.in;

/**
 * Input to {@link UpdateNewsItemUseCase}: the news item's GUID, its complete new content and its author.
 */
public record UpdateNewsItemCommand(String guid, String headline, String body, String language, String author) {

}
