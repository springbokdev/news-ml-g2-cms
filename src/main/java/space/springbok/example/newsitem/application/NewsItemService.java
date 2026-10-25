package space.springbok.example.newsitem.application;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import space.springbok.example.newsitem.application.port.in.CreateNewsItemCommand;
import space.springbok.example.newsitem.application.port.in.CreateNewsItemUseCase;
import space.springbok.example.newsitem.application.port.out.SaveArticleContentPort;
import space.springbok.example.newsitem.domain.model.ArticleContent;

@Service
class NewsItemService implements CreateNewsItemUseCase {

    private final SaveArticleContentPort saveArticleContentPort;
    private final Clock clock;
    private final String provider;

    NewsItemService(SaveArticleContentPort saveArticleContentPort, Clock clock,
                    @Value("${newsml.provider:springbok.space}") String provider) {
        this.saveArticleContentPort = saveArticleContentPort;
        this.clock = clock;
        this.provider = provider;
    }

    @Override
    public ArticleContent createNewsItem(CreateNewsItemCommand command) {
        ArticleContent articleContent = ArticleContent.create(
                command.headline(), command.body(), command.language(),
                provider, OffsetDateTime.now(clock), UUID.randomUUID().toString());
        saveArticleContentPort.save(articleContent);
        return articleContent;
    }

}
