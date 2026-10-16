package space.springbok.example.newsitem.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
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
        adapter.save(new ArticleContent(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en"));

        assertThat(repository.findAll())
                .singleElement()
                .satisfies(stored -> {
                    assertThat(stored.getId()).isNotNull();
                    assertThat(stored.getHeadline()).isEqualTo("Floods hit Cape Town");
                    assertThat(stored.getBody()).isEqualTo("Rain fell.\n\nRoads closed.\n\nSchools shut.");
                    assertThat(stored.getLanguage()).isEqualTo("en");
                });
    }
}
