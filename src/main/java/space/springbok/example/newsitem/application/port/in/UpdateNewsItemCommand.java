package space.springbok.example.newsitem.application.port.in;

/**
 * Input to {@link UpdateNewsItemUseCase}: the news item's GUID and its complete new content.
 */
public record UpdateNewsItemCommand(String guid, String headline, String body, String language) {

}
