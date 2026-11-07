package space.springbok.example.newsitem.application.port.in;

import java.util.List;
import space.springbok.example.newsitem.domain.model.ArticleContent;

/**
 * Inbound port: retrieve the modification history of a news item by its GUID.
 */
public interface GetNewsItemHistoryUseCase {

    /**
     * @return every version of the news item, oldest first
     */
    List<ArticleContent> getNewsItemHistory(String guid);

}
