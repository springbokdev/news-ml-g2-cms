package space.springbok.example.newsitem.application.port.in;

/**
 * Input to {@link CreateNewsItemUseCase}: the journalist's article content and its author.
 */
public record CreateNewsItemCommand(String headline, String body, String language, String author) {

}
