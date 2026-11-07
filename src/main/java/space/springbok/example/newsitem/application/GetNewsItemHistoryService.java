package space.springbok.example.newsitem.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import space.springbok.example.newsitem.application.port.in.GetNewsItemHistoryUseCase;
import space.springbok.example.newsitem.application.port.out.LoadNewsItemHistoryPort;
import space.springbok.example.newsitem.domain.model.ArticleContent;
import space.springbok.example.newsitem.domain.model.NewsItemGuid;
import space.springbok.example.newsitem.domain.model.NewsItemNotFoundException;

@Service
@RequiredArgsConstructor
class GetNewsItemHistoryService implements GetNewsItemHistoryUseCase {

    private final LoadNewsItemHistoryPort loadNewsItemHistoryPort;

    @Override
    public List<ArticleContent> getNewsItemHistory(String guid) {
        NewsItemGuid newsItemGuid = new NewsItemGuid(guid);
        List<ArticleContent> versions = loadNewsItemHistoryPort.findAllVersionsByGuid(newsItemGuid);
        if (versions.isEmpty()) {
            throw new NewsItemNotFoundException(newsItemGuid);
        }
        return versions;
    }

}
