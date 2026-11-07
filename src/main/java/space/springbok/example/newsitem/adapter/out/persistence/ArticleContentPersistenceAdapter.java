package space.springbok.example.newsitem.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import space.springbok.example.newsitem.application.port.out.LoadArticleContentPort;
import space.springbok.example.newsitem.application.port.out.LoadNewsItemHistoryPort;
import space.springbok.example.newsitem.application.port.out.SaveArticleContentPort;
import space.springbok.example.newsitem.domain.model.ArticleContent;
import space.springbok.example.newsitem.domain.model.NewsItemGuid;

@Component
@RequiredArgsConstructor
class ArticleContentPersistenceAdapter
        implements SaveArticleContentPort, LoadArticleContentPort, LoadNewsItemHistoryPort {

    private final ArticleContentJpaRepository repository;
    private final ArticleContentEntityMapper mapper;

    @Override
    public void save(ArticleContent content) {
        repository.save(mapper.toEntity(content));
    }

    @Override
    public Optional<ArticleContent> findByGuid(NewsItemGuid guid) {
        return repository.findFirstByGuidOrderByVersionDesc(guid.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<ArticleContent> findByGuidAndVersion(NewsItemGuid guid, int version) {
        return repository.findByGuidAndVersion(guid.value(), version).map(mapper::toDomain);
    }

    @Override
    public List<ArticleContent> findAllVersionsByGuid(NewsItemGuid guid) {
        return repository.findAllByGuidOrderByVersionAsc(guid.value()).stream().map(mapper::toDomain).toList();
    }
}
