package space.springbok.example.newsitem.application.port.in;

/**
 * Input to {@link UpdateNewsItemUseCase}: the news item's GUID, its complete new content, its author,
 * and the version the editor edited ({@code null} when the request did not name one).
 */
public record UpdateNewsItemCommand(String guid, String headline, String body, String language, String author,
                                    Integer version) {

}
