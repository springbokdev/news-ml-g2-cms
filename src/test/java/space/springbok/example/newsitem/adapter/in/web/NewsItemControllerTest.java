package space.springbok.example.newsitem.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import space.springbok.example.newsitem.application.port.in.CreateNewsItemCommand;
import space.springbok.example.newsitem.application.port.in.CreateNewsItemUseCase;
import space.springbok.example.newsitem.application.port.in.GetNewsItemUseCase;
import space.springbok.example.newsitem.domain.model.ArticleContent;
import space.springbok.example.newsitem.domain.model.MissingRequiredFieldException;
import space.springbok.example.newsitem.domain.model.NewsItemGuid;
import space.springbok.example.newsitem.domain.model.NewsItemNotFoundException;

@WebMvcTest(NewsItemController.class)
@Import({CreateNewsItemResponseMapperImpl.class, NewsItemXmlRenderer.class})
@DisplayName("News item web endpoint")
class NewsItemControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private CreateNewsItemUseCase createNewsItemUseCase;

    @MockitoBean
    private GetNewsItemUseCase getNewsItemUseCase;

    @Test
    @DisplayName("The one where a journalist submits a headline, body and language, and the article is created")
    void returnsCreatedWhenHeadlineBodyAndLanguageArePresent() {
        given(createNewsItemUseCase.createNewsItem(any(CreateNewsItemCommand.class)))
                .willReturn(ArticleContent.create(
                        "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                        "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e"));

        assertThat(mvc.post().uri("/api/news-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "headline": "Floods hit Cape Town",
                          "body": "Rain fell.\\n\\nRoads closed.\\n\\nSchools shut.",
                          "language": "en"
                        }
                        """))
                .hasStatus(HttpStatus.CREATED);

        then(createNewsItemUseCase).should().createNewsItem(new CreateNewsItemCommand(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en"));
    }

    @Test
    @DisplayName("The one where an article is created, and the response body carries the assigned GUID and version 1")
    void createdResponseCarriesGuidAndVersion() {
        given(createNewsItemUseCase.createNewsItem(any(CreateNewsItemCommand.class)))
                .willReturn(ArticleContent.create(
                        "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                        "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e"));

        assertThat(mvc.post().uri("/api/news-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "headline": "Floods hit Cape Town",
                          "body": "Rain fell.\\n\\nRoads closed.\\n\\nSchools shut.",
                          "language": "en"
                        }
                        """))
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.guid")
                            .isEqualTo("urn:newsml:springbok.space:20261016:3f2b9c1e");
                    assertThat(json).extractingPath("$.version").isEqualTo(1);
                });
    }

    @Test
    @DisplayName("The one where the journalist supplies their own GUID, and it is ignored in favour of the assigned one")
    void journalistSuppliedGuidIsIgnored() {
        given(createNewsItemUseCase.createNewsItem(any(CreateNewsItemCommand.class)))
                .willReturn(ArticleContent.create(
                        "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                        "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e"));

        assertThat(mvc.post().uri("/api/news-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "guid": "urn:newsml:example.org:20200101:my-own-guid",
                          "headline": "Floods hit Cape Town",
                          "body": "Rain fell.\\n\\nRoads closed.\\n\\nSchools shut.",
                          "language": "en"
                        }
                        """))
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .extractingPath("$.guid").isEqualTo("urn:newsml:springbok.space:20261016:3f2b9c1e");

        then(createNewsItemUseCase).should().createNewsItem(new CreateNewsItemCommand(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en"));
    }

    @Test
    @DisplayName("The one where the article is rejected, and the response is a 400 naming the missing field")
    void missingRequiredFieldIsBadRequestNamingTheField() {
        willThrow(new MissingRequiredFieldException("headline"))
                .given(createNewsItemUseCase).createNewsItem(any(CreateNewsItemCommand.class));

        assertThat(mvc.post().uri("/api/news-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "headline": "   ",
                          "body": "Rain fell.\\n\\nRoads closed.\\n\\nSchools shut.",
                          "language": "en"
                        }
                        """))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.field").isEqualTo("headline");
    }

    @Test
    @DisplayName("The one where an item exists, and fetching it by its GUID returns 200 with its NewsML-G2 XML")
    void existingNewsItemIsReturnedAsXml() {
        ArticleContent stored = ArticleContent.create(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e");
        given(getNewsItemUseCase.getNewsItem(stored.guid().value())).willReturn(stored);

        assertThat(mvc.get().uri("/api/news-items/{guid}", stored.guid().value())
                .accept(MediaType.APPLICATION_XML))
                .hasStatusOk()
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_XML)
                .bodyText()
                .contains("<headline>Floods hit Cape Town</headline>");
    }

    @Test
    @DisplayName("The one where the headline has non-ASCII characters, and the XML bytes are UTF-8 as the declaration says")
    void nonAsciiHeadlineIsServedAsUtf8Bytes() throws Exception {
        ArticleContent stored = ArticleContent.create(
                "Izikhukhula eKapa, café, 水", "Imvula ina kakhulu.", "zu-ZA",
                "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e");
        given(getNewsItemUseCase.getNewsItem(stored.guid().value())).willReturn(stored);

        MvcTestResult result = mvc.get().uri("/api/news-items/{guid}", stored.guid().value())
                .accept(MediaType.APPLICATION_XML)
                .exchange();

        assertThat(result).hasStatusOk();
        assertThat(new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8))
                .startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .contains("<headline>Izikhukhula eKapa, café, 水</headline>");
    }

    @Test
    @DisplayName("The one where a GUID that was never issued is requested, and the response is a 404 problem")
    void unknownGuidIsNotFound() {
        NewsItemGuid unissued = new NewsItemGuid("urn:newsml:springbok.space:20261016:never-issued");
        given(getNewsItemUseCase.getNewsItem(unissued.value())).willThrow(new NewsItemNotFoundException(unissued));

        assertThat(mvc.get().uri("/api/news-items/{guid}", unissued.value()))
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.title").isEqualTo("Not Found");
                    assertThat(json).extractingPath("$.status").isEqualTo(404);
                    assertThat(json).extractingPath("$.detail").isEqualTo("news item not found");
                    assertThat(json).extractingPath("$.instance")
                            .isEqualTo("/api/news-items/urn:newsml:springbok.space:20261016:never-issued");
                });
    }
}
