package space.springbok.example.newsitem.application.port.in;

/**
 * Inbound port: create a NewsML-G2 news item from a journalist's article content.
 */
public interface CreateNewsItemUseCase {

    void createNewsItem(CreateNewsItemCommand command);

}
