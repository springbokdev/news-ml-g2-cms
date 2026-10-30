package space.springbok.example.newsitem.application;

import java.time.Clock;
import java.time.OffsetDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import space.springbok.example.newsitem.application.port.in.UpdateNewsItemCommand;
import space.springbok.example.newsitem.application.port.in.UpdateNewsItemUseCase;
import space.springbok.example.newsitem.application.port.out.LoadArticleContentPort;
import space.springbok.example.newsitem.application.port.out.SaveArticleContentPort;
import space.springbok.example.newsitem.domain.model.ArticleContent;
import space.springbok.example.newsitem.domain.model.NewsItemGuid;
import space.springbok.example.newsitem.domain.model.NewsItemNotFoundException;

@Service
@RequiredArgsConstructor
class UpdateNewsItemService implements UpdateNewsItemUseCase {

    private final LoadArticleContentPort loadArticleContentPort;
    private final SaveArticleContentPort saveArticleContentPort;
    private final Clock clock;

    @Override
    public ArticleContent updateNewsItem(UpdateNewsItemCommand command) {
        NewsItemGuid guid = new NewsItemGuid(command.guid());
        ArticleContent latest = loadArticleContentPort.findByGuid(guid)
                .orElseThrow(() -> new NewsItemNotFoundException(guid));
        ArticleContent updated = latest.update(
                command.headline(), command.body(), command.language(), OffsetDateTime.now(clock));
        saveArticleContentPort.save(updated);
        return updated;
    }

}
