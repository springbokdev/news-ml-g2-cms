package space.springbok.example.newsitem.application.port.in;

import space.springbok.example.newsitem.domain.model.ArticleContent;

/**
 * Inbound port: create a NewsML-G2 news item from a journalist's article content.
 */
public interface CreateNewsItemUseCase {

    ArticleContent createNewsItem(CreateNewsItemCommand command);

}
