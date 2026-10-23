package space.springbok.example.newsitem.application.port.in;

import space.springbok.example.newsitem.domain.model.NewsItemGuid;

/**
 * Inbound port: create a NewsML-G2 news item from a journalist's article content.
 */
public interface CreateNewsItemUseCase {

    NewsItemGuid createNewsItem(CreateNewsItemCommand command);

}
