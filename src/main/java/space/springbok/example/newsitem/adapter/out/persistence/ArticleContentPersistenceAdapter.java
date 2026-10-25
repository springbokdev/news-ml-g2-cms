package space.springbok.example.newsitem.adapter.out.persistence;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import space.springbok.example.newsitem.application.port.out.LoadArticleContentPort;
import space.springbok.example.newsitem.application.port.out.SaveArticleContentPort;
import space.springbok.example.newsitem.domain.model.ArticleContent;
import space.springbok.example.newsitem.domain.model.NewsItemGuid;

@Component
@RequiredArgsConstructor
class ArticleContentPersistenceAdapter implements SaveArticleContentPort, LoadArticleContentPort {

    private final ArticleContentJpaRepository repository;
    private final ArticleContentEntityMapper mapper;

    @Override
    public void save(ArticleContent content) {
        repository.save(mapper.toEntity(content));
    }

    @Override
    public Optional<ArticleContent> findByGuid(NewsItemGuid guid) {
        return repository.findByGuid(guid.value()).map(mapper::toDomain);
    }
}
