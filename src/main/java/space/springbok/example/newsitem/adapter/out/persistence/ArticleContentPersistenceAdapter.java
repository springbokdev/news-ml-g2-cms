package space.springbok.example.newsitem.adapter.out.persistence;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import space.springbok.example.newsitem.application.port.out.LoadArticleContentPort;
import space.springbok.example.newsitem.application.port.out.LoadNewsItemHistoryPort;
import space.springbok.example.newsitem.application.port.out.SaveArticleContentPort;
import space.springbok.example.newsitem.domain.model.ArticleContent;
import space.springbok.example.newsitem.domain.model.NewsItemGuid;
import space.springbok.example.newsitem.domain.model.VersionConflictException;

@Component
@RequiredArgsConstructor
class ArticleContentPersistenceAdapter
        implements SaveArticleContentPort, LoadArticleContentPort, LoadNewsItemHistoryPort {

    /** The (guid, version) unique constraint from V7__store_every_version_of_a_news_item.sql. */
    private static final String GUID_VERSION_CONSTRAINT = "uq_article_content_guid_version";

    private final ArticleContentJpaRepository repository;
    private final ArticleContentEntityMapper mapper;

    /**
     * Stores the version, and rejects it as a {@link VersionConflictException} when another update
     * already stored the same version of the same news item since this one was checked.
     */
    @Override
    public void save(ArticleContent content) {
        try {
            repository.saveAndFlush(mapper.toEntity(content));
        } catch (DataIntegrityViolationException exception) {
            if (violatesGuidVersionConstraint(exception)) {
                throw new VersionConflictException();
            }
            throw exception;
        }
    }

    private static boolean violatesGuidVersionConstraint(DataIntegrityViolationException exception) {
        return exception.getCause() instanceof ConstraintViolationException violation
                && violation.getConstraintName() != null
                && violation.getConstraintName().toLowerCase(Locale.ROOT).contains(GUID_VERSION_CONSTRAINT);
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
