package space.springbok.example.newsitem.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.springbok.example.newsitem.application.port.in.UpdateNewsItemCommand;
import space.springbok.example.newsitem.application.port.out.LoadArticleContentPort;
import space.springbok.example.newsitem.application.port.out.SaveArticleContentPort;
import space.springbok.example.newsitem.domain.model.ArticleContent;
import space.springbok.example.newsitem.domain.model.NewsItemGuid;
import space.springbok.example.newsitem.domain.model.NewsItemNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Updating a news item")
class UpdateNewsItemServiceTest {

    @Mock
    private LoadArticleContentPort loadArticleContentPort;

    @Mock
    private SaveArticleContentPort saveArticleContentPort;

    @Captor
    private ArgumentCaptor<ArticleContent> articleContentCaptor;

    private final Clock clock = Clock.fixed(Instant.parse("2026-10-17T06:15:00Z"), ZoneId.of("Africa/Johannesburg"));

    @Test
    @DisplayName("The one where the item is at version 1 and the editor sends a new headline, and version 2 is saved and returned with the same GUID")
    void acceptedUpdateIsSavedAsTheNextVersion() {
        ArticleContent version1 = ArticleContent.create(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e");
        given(loadArticleContentPort.findByGuid(version1.guid())).willReturn(Optional.of(version1));
        UpdateNewsItemService service = new UpdateNewsItemService(loadArticleContentPort, saveArticleContentPort, clock);

        ArticleContent returned = service.updateNewsItem(new UpdateNewsItemCommand(
                version1.guid().value(),
                "Floods hit Cape Town, two dead", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en"));

        then(saveArticleContentPort).should().save(articleContentCaptor.capture());
        ArticleContent saved = articleContentCaptor.getValue();
        assertThat(saved.guid()).isEqualTo(version1.guid());
        assertThat(saved.version()).isEqualTo(2);
        assertThat(saved.headline()).isEqualTo("Floods hit Cape Town, two dead");
        assertThat(saved.versionCreated()).isEqualTo(OffsetDateTime.parse("2026-10-17T08:15:00+02:00"));
        assertThat(returned).isEqualTo(saved);
    }

    @Test
    @DisplayName("The one where the editor updates a GUID that was never issued, and the news item is not found and nothing is saved")
    void neverIssuedGuidIsNotFoundAndNothingIsSaved() {
        NewsItemGuid unissued = new NewsItemGuid("urn:newsml:springbok.space:20261016:never-issued");
        given(loadArticleContentPort.findByGuid(unissued)).willReturn(Optional.empty());
        UpdateNewsItemService service = new UpdateNewsItemService(loadArticleContentPort, saveArticleContentPort, clock);

        assertThatThrownBy(() -> service.updateNewsItem(new UpdateNewsItemCommand(
                unissued.value(),
                "Floods hit Cape Town, two dead", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en")))
                .isInstanceOf(NewsItemNotFoundException.class)
                .extracting("guid")
                .isEqualTo(unissued);
        then(saveArticleContentPort).shouldHaveNoInteractions();
    }
}
