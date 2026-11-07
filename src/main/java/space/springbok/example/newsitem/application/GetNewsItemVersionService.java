package space.springbok.example.newsitem.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import space.springbok.example.newsitem.application.port.in.GetNewsItemVersionUseCase;
import space.springbok.example.newsitem.application.port.out.LoadArticleContentPort;
import space.springbok.example.newsitem.domain.model.ArticleContent;
import space.springbok.example.newsitem.domain.model.NewsItemGuid;
import space.springbok.example.newsitem.domain.model.NewsItemNotFoundException;

@Service
@RequiredArgsConstructor
class GetNewsItemVersionService implements GetNewsItemVersionUseCase {

    private final LoadArticleContentPort loadArticleContentPort;

    @Override
    public ArticleContent getNewsItemVersion(String guid, int version) {
        NewsItemGuid newsItemGuid = new NewsItemGuid(guid);
        return loadArticleContentPort.findByGuidAndVersion(newsItemGuid, version)
                .orElseThrow(() -> new NewsItemNotFoundException(newsItemGuid));
    }

}
