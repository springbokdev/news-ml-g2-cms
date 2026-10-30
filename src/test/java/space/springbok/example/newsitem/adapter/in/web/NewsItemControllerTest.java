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
import space.springbok.example.newsitem.application.port.in.UpdateNewsItemCommand;
import space.springbok.example.newsitem.application.port.in.UpdateNewsItemUseCase;
import space.springbok.example.newsitem.domain.model.ArticleContent;
import space.springbok.example.newsitem.domain.model.MissingRequiredFieldException;
import space.springbok.example.newsitem.domain.model.NewsItemGuid;
import space.springbok.example.newsitem.domain.model.NewsItemNotFoundException;

@WebMvcTest(NewsItemController.class)
@Import({CreateNewsItemResponseMapperImpl.class, UpdateNewsItemResponseMapperImpl.class, NewsItemXmlRenderer.class})
@DisplayName("News item web endpoint")
class NewsItemControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private CreateNewsItemUseCase createNewsItemUseCase;

    @MockitoBean
    private GetNewsItemUseCase getNewsItemUseCase;

    @MockitoBean
    private UpdateNewsItemUseCase updateNewsItemUseCase;

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

    @Test
    @DisplayName("The one where a client accepts only JSON when fetching a news item, and the response is a 406")
    void clientAcceptingOnlyJsonIsNotAcceptable() {
        assertThat(mvc.get().uri("/api/news-items/{guid}", "urn:newsml:springbok.space:20261016:3f2b9c1e")
                .accept(MediaType.APPLICATION_JSON))
                .hasStatus(HttpStatus.NOT_ACCEPTABLE);

        then(getNewsItemUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("The one where the create request body is not readable JSON, and the response is a 400")
    void malformedJsonIsBadRequest() {
        assertThat(mvc.post().uri("/api/news-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"headline\": \"Floods hit Cape Town\", "))
                .hasStatus(HttpStatus.BAD_REQUEST);

        then(createNewsItemUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("The one where an editor sends a new headline for an existing item, and the update is accepted as version 2 with the same GUID")
    void returnsOkWithGuidAndNextVersionWhenUpdateIsAccepted() {
        ArticleContent version1 = ArticleContent.create(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e");
        given(updateNewsItemUseCase.updateNewsItem(any(UpdateNewsItemCommand.class)))
                .willReturn(version1.update(
                        "Floods hit Cape Town, two dead", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                        "Thandi Nkosi", OffsetDateTime.parse("2026-10-17T08:15:00+02:00")));

        assertThat(mvc.put().uri("/api/news-items/{guid}", version1.guid().value())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "headline": "Floods hit Cape Town, two dead",
                          "body": "Rain fell.\\n\\nRoads closed.\\n\\nSchools shut.",
                          "language": "en",
                          "author": "Thandi Nkosi",
                          "version": 1
                        }
                        """))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.guid").isEqualTo(version1.guid().value());
                    assertThat(json).extractingPath("$.version").isEqualTo(2);
                });

        then(updateNewsItemUseCase).should().updateNewsItem(new UpdateNewsItemCommand(
                version1.guid().value(),
                "Floods hit Cape Town, two dead", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                "Thandi Nkosi"));
    }
}
