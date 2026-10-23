package space.springbok.example.newsitem.application.port.out;

import java.util.Optional;
import space.springbok.example.newsitem.domain.model.ArticleContent;
import space.springbok.example.newsitem.domain.model.NewsItemGuid;

/**
 * Outbound port: load a journalist's article content by the GUID of its news item.
 */
public interface LoadArticleContentPort {

    Optional<ArticleContent> findByGuid(NewsItemGuid guid);

}
