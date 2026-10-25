package space.springbok.example.newsitem.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import space.springbok.example.newsitem.application.port.in.GetNewsItemUseCase;
import space.springbok.example.newsitem.application.port.out.LoadArticleContentPort;
import space.springbok.example.newsitem.domain.model.ArticleContent;
import space.springbok.example.newsitem.domain.model.NewsItemGuid;
import space.springbok.example.newsitem.domain.model.NewsItemNotFoundException;

@Service
@RequiredArgsConstructor
class GetNewsItemService implements GetNewsItemUseCase {

    private final LoadArticleContentPort loadArticleContentPort;

    @Override
    public ArticleContent getNewsItem(String guid) {
        NewsItemGuid newsItemGuid = new NewsItemGuid(guid);
        return loadArticleContentPort.findByGuid(newsItemGuid)
                .orElseThrow(() -> new NewsItemNotFoundException(newsItemGuid));
    }

}
