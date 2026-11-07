package space.springbok.example.newsitem.application.port.in;

import space.springbok.example.newsitem.domain.model.ArticleContent;

/**
 * Inbound port: retrieve one version of a news item, exactly as it was stored.
 */
public interface GetNewsItemVersionUseCase {

    ArticleContent getNewsItemVersion(String guid, int version);

}
