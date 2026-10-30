package space.springbok.example.newsitem.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import space.springbok.example.newsitem.domain.model.ArticleContent;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ArticleContentPersistenceAdapter.class, ArticleContentEntityMapperImpl.class})
@DisplayName("Persisting article content")
class ArticleContentPersistenceAdapterTest {

    @Autowired
    private ArticleContentPersistenceAdapter adapter;

    @Autowired
    private ArticleContentJpaRepository repository;

    @Test
    @DisplayName("The one where article content is saved, and its headline, body and language are stored")
    void savedArticleContentIsStored() {
        adapter.save(ArticleContent.create(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e"));

        assertThat(repository.findAll())
                .singleElement()
                .satisfies(stored -> {
                    assertThat(stored.getId()).isNotNull();
                    assertThat(stored.getHeadline()).isEqualTo("Floods hit Cape Town");
                    assertThat(stored.getBody()).isEqualTo("Rain fell.\n\nRoads closed.\n\nSchools shut.");
                    assertThat(stored.getLanguage()).isEqualTo("en");
                });
    }

    @Test
    @DisplayName("The one where article content is saved, and its GUID is stored")
    void savedArticleContentGuidIsStored() {
        adapter.save(ArticleContent.create(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e"));

        assertThat(repository.findAll())
                .singleElement()
                .extracting(ArticleContentEntity::getGuid)
                .isEqualTo("urn:newsml:springbok.space:20261016:3f2b9c1e");
    }

    @Test
    @DisplayName("The one where article content is saved, and its version 1 is stored")
    void savedArticleContentVersionIsStored() {
        adapter.save(ArticleContent.create(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e"));

        assertThat(repository.findAll())
                .singleElement()
                .extracting(ArticleContentEntity::getVersion)
                .isEqualTo(1);
    }

    @Test
    @DisplayName("The one where article content created at 2026-03-01T14:05:09-05:00 is saved and loaded by its GUID, and its versionCreated keeps that instant and offset")
    void loadedArticleContentKeepsItsVersionCreatedWithOffset() {
        ArticleContent saved = ArticleContent.create(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                "springbok.space", OffsetDateTime.parse("2026-03-01T14:05:09-05:00"), "3f2b9c1e");
        adapter.save(saved);
        repository.flush();

        assertThat(adapter.findByGuid(saved.guid()))
                .get()
                .extracting(ArticleContent::versionCreated)
                .isEqualTo(OffsetDateTime.parse("2026-03-01T14:05:09-05:00"));
    }

    @Test
    @DisplayName("The one where an item has versions 1 and 2 stored, and loading it by its GUID returns version 2 while version 1 stays stored")
    void loadingByGuidReturnsTheLatestVersionAndKeepsTheEarlierOne() {
        ArticleContent version1 = ArticleContent.create(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e");
        ArticleContent version2 = version1.update(
                "Floods hit Cape Town, two dead", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                OffsetDateTime.parse("2026-10-17T08:15:00+02:00"));
        adapter.save(version1);
        adapter.save(version2);
        repository.flush();

        assertThat(adapter.findByGuid(version1.guid())).contains(version2);
        assertThat(repository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("The one where article content has no versionCreated, and the database rejects it")
    void versionCreatedIsRequired() {
        ArticleContentEntity withoutVersionCreated = new ArticleContentEntity();
        withoutVersionCreated.setGuid("urn:newsml:springbok.space:20261016:3f2b9c1e");
        withoutVersionCreated.setHeadline("Floods hit Cape Town");
        withoutVersionCreated.setBody("Rain fell.");
        withoutVersionCreated.setLanguage("en");
        withoutVersionCreated.setVersion(1);

        assertThatThrownBy(() -> repository.saveAndFlush(withoutVersionCreated))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("The one where two articles share a GUID, and the second one is rejected by the database")
    void duplicateGuidIsRejected() {
        ArticleContentEntity first = new ArticleContentEntity();
        first.setGuid("urn:newsml:springbok.space:20261016:3f2b9c1e");
        first.setHeadline("Floods hit Cape Town");
        first.setBody("Rain fell.");
        first.setLanguage("en");
        first.setVersionCreated(OffsetDateTime.parse("2026-10-16T09:30:00+02:00"));
        repository.saveAndFlush(first);

        ArticleContentEntity second = new ArticleContentEntity();
        second.setGuid("urn:newsml:springbok.space:20261016:3f2b9c1e");
        second.setHeadline("Floods hit Cape Town");
        second.setBody("Rain fell.");
        second.setLanguage("en");
        second.setVersionCreated(OffsetDateTime.parse("2026-10-16T09:30:00+02:00"));

        assertThatThrownBy(() -> repository.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
