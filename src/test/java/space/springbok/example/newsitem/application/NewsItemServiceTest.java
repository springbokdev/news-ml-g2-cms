package space.springbok.example.newsitem.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.springbok.example.newsitem.application.port.in.CreateNewsItemCommand;
import space.springbok.example.newsitem.application.port.out.SaveArticleContentPort;
import space.springbok.example.newsitem.domain.model.ArticleContent;
import space.springbok.example.newsitem.domain.model.MissingRequiredFieldException;
import space.springbok.example.newsitem.domain.model.NewsItemGuid;

@ExtendWith(MockitoExtension.class)
@DisplayName("Creating a news item")
class NewsItemServiceTest {

    @Mock
    private SaveArticleContentPort saveArticleContentPort;

    @Captor
    private ArgumentCaptor<ArticleContent> articleContentCaptor;

    private final Clock clock = Clock.fixed(Instant.parse("2026-10-16T07:30:00Z"), ZoneId.of("Africa/Johannesburg"));

    @Test
    @DisplayName("The one where a valid article is created, and its content is saved")
    void validArticleContentIsSaved() {
        NewsItemService service = new NewsItemService(saveArticleContentPort, clock, "springbok.space");

        service.createNewsItem(new CreateNewsItemCommand(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en"));

        then(saveArticleContentPort).should().save(articleContentCaptor.capture());
        assertThat(articleContentCaptor.getValue())
                .extracting(ArticleContent::headline, ArticleContent::body, ArticleContent::language)
                .containsExactly("Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en");
    }

    @Test
    @DisplayName("The one where an article is created, and the GUID of the saved article is returned")
    void createdArticleGuidIsReturned() {
        NewsItemService service = new NewsItemService(saveArticleContentPort, clock, "springbok.space");

        NewsItemGuid guid = service.createNewsItem(new CreateNewsItemCommand(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en"));

        then(saveArticleContentPort).should().save(articleContentCaptor.capture());
        assertThat(guid).isEqualTo(articleContentCaptor.getValue().guid());
    }

    @Test
    @DisplayName("The one where an article is created on 16 Oct 2026, and its GUID carries the date 20261016")
    void guidCarriesCreationDateFromClock() {
        NewsItemService service = new NewsItemService(saveArticleContentPort, clock, "springbok.space");

        NewsItemGuid guid = service.createNewsItem(new CreateNewsItemCommand(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en"));

        assertThat(guid.value()).startsWith("urn:newsml:springbok.space:20261016:");
    }

    @Test
    @DisplayName("The one where two articles with the same headline are created on the same day, and they get different GUIDs")
    void sameHeadlineSameDayGetsDifferentGuids() {
        NewsItemService service = new NewsItemService(saveArticleContentPort, clock, "springbok.space");
        CreateNewsItemCommand command = new CreateNewsItemCommand(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en");

        NewsItemGuid first = service.createNewsItem(command);
        NewsItemGuid second = service.createNewsItem(command);

        assertThat(second).isNotEqualTo(first);
    }

    @Test
    @DisplayName("The one where the provider is configured as example.org, and the GUID carries that provider")
    void guidCarriesConfiguredProvider() {
        NewsItemService service = new NewsItemService(saveArticleContentPort, clock, "example.org");

        NewsItemGuid guid = service.createNewsItem(new CreateNewsItemCommand(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en"));

        assertThat(guid.value()).startsWith("urn:newsml:example.org:20261016:");
    }

    @Test
    @DisplayName("The one where the headline is blank, and creating the news item is rejected: headline required")
    void blankHeadlineIsRejectedOnCreate() {
        NewsItemService service = new NewsItemService(saveArticleContentPort, clock, "springbok.space");
        CreateNewsItemCommand command = new CreateNewsItemCommand(
                "   ", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en");

        assertThatThrownBy(() -> service.createNewsItem(command))
                .isInstanceOf(MissingRequiredFieldException.class)
                .extracting("field")
                .isEqualTo("headline");
        then(saveArticleContentPort).should(never()).save(any());
    }
}
