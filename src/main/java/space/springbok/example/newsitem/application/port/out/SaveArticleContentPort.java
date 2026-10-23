package space.springbok.example.newsitem.application.port.out;

import space.springbok.example.newsitem.domain.model.ArticleContent;

/**
 * Outbound port: persist a journalist's article content.
 */
public interface SaveArticleContentPort {

    void save(ArticleContent content);

}
