package space.springbok.example.newsitem.application;

import org.springframework.stereotype.Service;
import space.springbok.example.newsitem.application.port.in.CreateNewsItemCommand;
import space.springbok.example.newsitem.application.port.in.CreateNewsItemUseCase;
import space.springbok.example.newsitem.application.port.out.SaveArticleContentPort;
import space.springbok.example.newsitem.domain.model.ArticleContent;

@Service
class NewsItemService implements CreateNewsItemUseCase {

    private final SaveArticleContentPort saveArticleContentPort;

    NewsItemService(SaveArticleContentPort saveArticleContentPort) {
        this.saveArticleContentPort = saveArticleContentPort;
    }

    @Override
    public void createNewsItem(CreateNewsItemCommand command) {
        saveArticleContentPort.save(
                new ArticleContent(command.headline(), command.body(), command.language()));
    }

}
