package space.springbok.example.newsitem.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.springbok.example.newsitem.application.port.out.LoadArticleContentPort;
import space.springbok.example.newsitem.domain.model.ArticleContent;
import space.springbok.example.newsitem.domain.model.MalformedNewsItemGuidException;
import space.springbok.example.newsitem.domain.model.NewsItemGuid;
import space.springbok.example.newsitem.domain.model.NewsItemNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Retrieving a news item")
class GetNewsItemServiceTest {

    @Mock
    private LoadArticleContentPort loadArticleContentPort;

    @Test
    @DisplayName("The one where an item was created, and fetching it by its GUID returns its article content")
    void existingNewsItemIsReturnedByGuid() {
        ArticleContent stored = ArticleContent.create(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e");
        given(loadArticleContentPort.findByGuid(stored.guid())).willReturn(Optional.of(stored));
        GetNewsItemService service = new GetNewsItemService(loadArticleContentPort);

        assertThat(service.getNewsItem(stored.guid().value())).isEqualTo(stored);
    }

    @Test
    @DisplayName("The one where a GUID that was never issued is requested, and the news item is not found")
    void unknownGuidIsNotFound() {
        NewsItemGuid unissued = new NewsItemGuid("urn:newsml:springbok.space:20261016:never-issued");
        given(loadArticleContentPort.findByGuid(unissued)).willReturn(Optional.empty());
        GetNewsItemService service = new GetNewsItemService(loadArticleContentPort);

        assertThatThrownBy(() -> service.getNewsItem(unissued.value()))
                .isInstanceOf(NewsItemNotFoundException.class)
                .extracting("guid")
                .isEqualTo(unissued);
    }

    @Test
    @DisplayName("The one where the requested GUID is malformed, and it is rejected without looking anything up")
    void malformedGuidIsRejectedWithoutLookup() {
        GetNewsItemService service = new GetNewsItemService(loadArticleContentPort);

        assertThatThrownBy(() -> service.getNewsItem("abc"))
                .isInstanceOf(MalformedNewsItemGuidException.class);
        then(loadArticleContentPort).shouldHaveNoInteractions();
    }
}
