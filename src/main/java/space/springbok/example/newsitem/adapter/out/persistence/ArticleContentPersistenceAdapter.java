package space.springbok.example.newsitem.adapter.out.persistence;

import org.springframework.stereotype.Component;
import space.springbok.example.newsitem.application.port.out.SaveArticleContentPort;
import space.springbok.example.newsitem.domain.model.ArticleContent;

@Component
class ArticleContentPersistenceAdapter implements SaveArticleContentPort {

    private final ArticleContentJpaRepository repository;
    private final ArticleContentEntityMapper mapper;

    ArticleContentPersistenceAdapter(ArticleContentJpaRepository repository, ArticleContentEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void save(ArticleContent content) {
        repository.save(mapper.toEntity(content));
    }
}
