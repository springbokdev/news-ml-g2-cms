package space.springbok.example.newsitem.application.port.in;

/**
 * Input to {@link CreateNewsItemUseCase}: the journalist's article content.
 */
public record CreateNewsItemCommand(String headline, String body, String language) {

}
