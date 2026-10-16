package space.springbok.example.acceptance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
}
