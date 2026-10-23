package space.springbok.example.acceptance;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Acceptance tests for doc/specs/create-news-item.md
 * "As a journalist, I want to create a NewsML-G2 news item
 * so that I can publish text-based news content to partner organisations."
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Create a NewsML-G2 news item")
class NewsItemAcceptanceIT {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Must require a headline, a body and a language to create an article")
    class MustRequireHeadlineBodyAndLanguageToCreateAnArticle {

        @Test
        @DisplayName("The one where a journalist submits a headline, body and language, and the article is created")
        void journalistSubmitsHeadlineBodyAndLanguage() throws Exception {
            String requestBody = """
                    {
                      "headline": "Floods hit Cape Town",
                      "body": "Rain fell.\\n\\nRoads closed.\\n\\nSchools shut.",
                      "language": "en"
                    }
                    """;

            mockMvc.perform(post("/api/news-items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("The one where the headline is blank, and the item is rejected with a reason")
        void headlineIsBlank() throws Exception {
            String requestBody = """
                    {
                      "headline": "   ",
                      "body": "Rain fell.\\n\\nRoads closed.\\n\\nSchools shut.",
                      "language": "en"
                    }
                    """;

            mockMvc.perform(post("/api/news-items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.field").value("headline"));
        }

        @Test
        @DisplayName("The one where the body is blank, and the item is rejected")
        void bodyIsBlank() throws Exception {
            String requestBody = """
                    {
                      "headline": "Floods hit Cape Town",
                      "body": "",
                      "language": "en"
                    }
                    """;

            mockMvc.perform(post("/api/news-items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.field").value("body"));
        }

        @Test
        @DisplayName("The one where the language is missing, and the item is rejected")
        void languageIsMissing() throws Exception {
            String requestBody = """
                    {
                      "headline": "Floods hit Cape Town",
                      "body": "Rain fell.\\n\\nRoads closed.\\n\\nSchools shut."
                    }
                    """;

            mockMvc.perform(post("/api/news-items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.field").value("language"));
        }
    }

    @Nested
    @DisplayName("Must accept the language only as a well-formed BCP 47 tag")
    class MustAcceptLanguageOnlyAsWellFormedBcp47Tag {

        @Test
        @DisplayName("The one where the language is en, and the article is accepted")
        void languageIsEn() throws Exception {
            String requestBody = """
                    {
                      "headline": "Floods hit Cape Town",
                      "body": "Rain fell.\\n\\nRoads closed.\\n\\nSchools shut.",
                      "language": "en"
                    }
                    """;

            mockMvc.perform(post("/api/news-items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("The one where the language is zu-ZA (language plus region), and the article is accepted")
        void languageIsZuZa() throws Exception {
            String requestBody = """
                    {
                      "headline": "Floods hit Cape Town",
                      "body": "Rain fell.\\n\\nRoads closed.\\n\\nSchools shut.",
                      "language": "zu-ZA"
                    }
                    """;

            mockMvc.perform(post("/api/news-items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("The one where the language is english, and the article is rejected: not a valid language tag")
        void languageIsEnglish() throws Exception {
            String requestBody = """
                    {
                      "headline": "Floods hit Cape Town",
                      "body": "Rain fell.\\n\\nRoads closed.\\n\\nSchools shut.",
                      "language": "english"
                    }
                    """;

            mockMvc.perform(post("/api/news-items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.field").value("language"))
                    .andExpect(jsonPath("$.detail").value("language is not a valid language tag"));
        }
    }

    /** Pins "today" to 16 Oct 2026 so the date part of the GUID is deterministic. */
    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-10-16T07:30:00Z"), ZoneId.of("Africa/Johannesburg"));
        }
    }

    @Nested
    @Import(FixedClockConfig.class)
    @DisplayName("Must give every new news item a globally unique GUID")
    class MustGiveEveryNewNewsItemGloballyUniqueGuid {

        private static final String ARTICLE = """
                {
                  "headline": "Floods hit Cape Town",
                  "body": "Rain fell.\\n\\nRoads closed.\\n\\nSchools shut.",
                  "language": "en"
                }
                """;

        @Test
        @DisplayName("The one where an article is created on 16 Oct 2026 by provider springbok.space, and its GUID is urn:newsml:springbok.space:20261016:<unique-id>")
        void guidIsMadeFromProviderCreationDateAndUniqueId() throws Exception {
            mockMvc.perform(post("/api/news-items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ARTICLE))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.guid")
                            .value(matchesPattern("^urn:newsml:springbok\\.space:20261016:[^:]+$")));
        }

        @Test
        @DisplayName("The one where two articles with the same headline are created on the same day, and they get different GUIDs")
        void sameHeadlineSameDayGetsDifferentGuids() throws Exception {
            String first = createAndReadGuid(ARTICLE);
            String second = createAndReadGuid(ARTICLE);

            assertThat(first).isNotBlank();
            assertThat(second).isNotBlank().isNotEqualTo(first);
        }

        @Test
        @DisplayName("The one where the journalist supplies their own GUID, and the system ignores it and assigns its own")
        void journalistSuppliedGuidIsIgnored() throws Exception {
            String suppliedGuid = "urn:newsml:example.org:20200101:my-own-guid";
            String requestBody = """
                    {
                      "guid": "%s",
                      "headline": "Floods hit Cape Town",
                      "body": "Rain fell.\\n\\nRoads closed.\\n\\nSchools shut.",
                      "language": "en"
                    }
                    """.formatted(suppliedGuid);

            String assigned = createAndReadGuid(requestBody);

            assertThat(assigned)
                    .isNotEqualTo(suppliedGuid)
                    .matches("^urn:newsml:springbok\\.space:20261016:[^:]+$");
        }

        private String createAndReadGuid(String requestBody) throws Exception {
            String response = mockMvc.perform(post("/api/news-items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            return JsonPath.read(response, "$.guid");
        }
    }

    @Nested
    @DisplayName("Must give a newly created news item version 1")
    class MustGiveNewlyCreatedNewsItemVersionOne {

        @Test
        @DisplayName("The one where a new article is created, and its version is 1")
        void newArticleHasVersionOne() throws Exception {
            String requestBody = """
                    {
                      "headline": "Floods hit Cape Town",
                      "body": "Rain fell.\\n\\nRoads closed.\\n\\nSchools shut.",
                      "language": "en"
                    }
                    """;

            mockMvc.perform(post("/api/news-items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.version").value(1));
        }

        @Test
        @DisplayName("The one where the journalist supplies version 5 on creation, and the item still gets version 1")
        void journalistSuppliedVersionIsIgnored() throws Exception {
            String requestBody = """
                    {
                      "version": 5,
                      "headline": "Floods hit Cape Town",
                      "body": "Rain fell.\\n\\nRoads closed.\\n\\nSchools shut.",
                      "language": "en"
                    }
                    """;

            mockMvc.perform(post("/api/news-items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.version").value(1));
        }
    }
}
