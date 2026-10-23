package space.springbok.example.newsitem.application;

import org.springframework.stereotype.Service;
import space.springbok.example.newsitem.application.port.in.GetNewsItemUseCase;
import space.springbok.example.newsitem.application.port.out.LoadArticleContentPort;
import space.springbok.example.newsitem.domain.model.ArticleContent;
import space.springbok.example.newsitem.domain.model.NewsItemGuid;
import space.springbok.example.newsitem.domain.model.NewsItemNotFoundException;

@Service
class GetNewsItemService implements GetNewsItemUseCase {

    private final LoadArticleContentPort loadArticleContentPort;

    GetNewsItemService(LoadArticleContentPort loadArticleContentPort) {
        this.loadArticleContentPort = loadArticleContentPort;
    }

    @Override
    public ArticleContent getNewsItem(NewsItemGuid guid) {
        return loadArticleContentPort.findByGuid(guid).orElseThrow(() -> new NewsItemNotFoundException(guid));
    }

}
