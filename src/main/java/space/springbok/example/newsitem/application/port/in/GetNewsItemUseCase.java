package space.springbok.example.newsitem.application.port.in;

import space.springbok.example.newsitem.domain.model.ArticleContent;

/**
 * Inbound port: retrieve a news item's article content by its GUID.
 */
public interface GetNewsItemUseCase {

    ArticleContent getNewsItem(String guid);

}
