package space.springbok.example.newsitem.application.port.out;

import java.util.Optional;
import space.springbok.example.newsitem.domain.model.ArticleContent;
import space.springbok.example.newsitem.domain.model.NewsItemGuid;

/**
 * Outbound port: load a journalist's article content by the GUID of its news item.
 */
public interface LoadArticleContentPort {

    /**
     * @return the latest version of the news item
     */
    Optional<ArticleContent> findByGuid(NewsItemGuid guid);

    /**
     * @return the given version of the news item, exactly as it was stored; empty if the item has no such version
     */
    Optional<ArticleContent> findByGuidAndVersion(NewsItemGuid guid, int version);

}
