package space.springbok.example.newsitem.application.port.in;

import space.springbok.example.newsitem.domain.model.ArticleContent;

/**
 * Inbound port: save an editor's update as a new version of a news item.
 */
public interface UpdateNewsItemUseCase {

    ArticleContent updateNewsItem(UpdateNewsItemCommand command);

}
