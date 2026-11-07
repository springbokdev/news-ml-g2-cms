package space.springbok.example.newsitem.application.port.in;

/**
 * Input to {@link UpdateNewsItemUseCase}: the news item's GUID, its complete new content, its author,
 * and the version the editor edited.
 */
public record UpdateNewsItemCommand(String guid, String headline, String body, String language, String author,
                                    int version) {

}
