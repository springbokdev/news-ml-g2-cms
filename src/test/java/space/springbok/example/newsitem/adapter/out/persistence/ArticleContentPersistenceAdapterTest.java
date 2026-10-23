package space.springbok.example.newsitem.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
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
                "springbok.space", LocalDate.of(2026, 10, 16), "3f2b9c1e"));

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
                "springbok.space", LocalDate.of(2026, 10, 16), "3f2b9c1e"));

        assertThat(repository.findAll())
                .singleElement()
                .extracting(ArticleContentEntity::getGuid)
                .isEqualTo("urn:newsml:springbok.space:20261016:3f2b9c1e");
    }

    @Test
    @DisplayName("The one where two articles share a GUID, and the second one is rejected by the database")
    void duplicateGuidIsRejected() {
        ArticleContentEntity first = new ArticleContentEntity();
        first.setGuid("urn:newsml:springbok.space:20261016:3f2b9c1e");
        first.setHeadline("Floods hit Cape Town");
        first.setBody("Rain fell.");
        first.setLanguage("en");
        repository.saveAndFlush(first);

        ArticleContentEntity second = new ArticleContentEntity();
        second.setGuid("urn:newsml:springbok.space:20261016:3f2b9c1e");
        second.setHeadline("Floods hit Cape Town");
        second.setBody("Rain fell.");
        second.setLanguage("en");

        assertThatThrownBy(() -> repository.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
