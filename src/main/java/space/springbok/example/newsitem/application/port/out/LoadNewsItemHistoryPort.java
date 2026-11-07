package space.springbok.example.newsitem.application.port.out;

import java.util.List;
import space.springbok.example.newsitem.domain.model.ArticleContent;
import space.springbok.example.newsitem.domain.model.NewsItemGuid;

/**
 * Outbound port: load every stored version of a news item by its GUID.
 */
public interface LoadNewsItemHistoryPort {

    /**
     * @return all versions of the news item, oldest first; empty if no news item has this GUID
     */
    List<ArticleContent> findAllVersionsByGuid(NewsItemGuid guid);

}
