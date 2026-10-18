package space.springbok.example.newsitem.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import space.springbok.example.newsitem.application.port.in.CreateNewsItemCommand;
import space.springbok.example.newsitem.application.port.in.CreateNewsItemUseCase;
import space.springbok.example.newsitem.domain.model.InvalidLanguageTagException;
import space.springbok.example.newsitem.domain.model.MissingRequiredFieldException;

@WebMvcTest(NewsItemController.class)
@DisplayName("News item web endpoint")
class NewsItemControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private CreateNewsItemUseCase createNewsItemUseCase;

    @Test
    @DisplayName("The one where a journalist submits a headline, body and language, and the article is created")
    void returnsCreatedWhenHeadlineBodyAndLanguageArePresent() {
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
    @DisplayName("The one where the language is not a valid language tag, and the response is a 400 naming the language")
    void invalidLanguageTagIsBadRequestNamingTheField() {
        willThrow(new InvalidLanguageTagException("language"))
                .given(createNewsItemUseCase).createNewsItem(any(CreateNewsItemCommand.class));

        assertThat(mvc.post().uri("/api/news-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "headline": "Floods hit Cape Town",
                          "body": "Rain fell.\\n\\nRoads closed.\\n\\nSchools shut.",
                          "language": "english"
                        }
                        """))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.field").isEqualTo("language");
    }
}
