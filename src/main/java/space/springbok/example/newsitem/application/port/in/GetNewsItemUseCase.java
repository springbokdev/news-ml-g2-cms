package space.springbok.example.newsitem.application.port.in;

import space.springbok.example.newsitem.domain.model.ArticleContent;
import space.springbok.example.newsitem.domain.model.NewsItemGuid;

/**
 * Inbound port: retrieve a news item's article content by its GUID.
 */
public interface GetNewsItemUseCase {

    ArticleContent getNewsItem(NewsItemGuid guid);

}
