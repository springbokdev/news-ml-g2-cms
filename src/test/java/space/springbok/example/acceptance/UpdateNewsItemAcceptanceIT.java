package space.springbok.example.acceptance;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

/**
 * Acceptance tests for doc/specs/update-news-item.md
 * "As an editor, I want to update an existing news item
 * so that corrections and updates can be distributed."
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Update a news item")
class UpdateNewsItemAcceptanceIT {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Must save an accepted update as a new version of the same news item")
    class MustSaveAnAcceptedUpdateAsANewVersionOfTheSameNewsItem {

        private static final Map<String, String> NAMESPACES = Map.of(
                "n", "http://iptc.org/std/nar/2006-10-01/",
                "x", "http://www.w3.org/1999/xhtml");

        @Test
        @DisplayName("The one where the item is at version 1 with headline \"Floods hit Cape Town\", and the editor sends headline \"Floods hit Cape Town, two dead\" with the same body and en. The item is now version 2 with the new headline and the same GUID")
        void acceptedUpdateBecomesVersionTwoWithTheSameGuid() throws Exception {
            String createResponse = mockMvc.perform(post("/api/news-items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "headline": "Floods hit Cape Town",
                                      "body": "Rain fell.\\n\\nRoads closed.\\n\\nSchools shut.",
                                      "language": "en"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            String guid = JsonPath.read(createResponse, "$.guid");

            mockMvc.perform(put("/api/news-items/{guid}", guid)
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
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.guid").value(guid))
                    .andExpect(jsonPath("$.version").value(2));

            String paragraphs = "/n:newsItem/n:contentSet/n:inlineXML/x:html/x:body/x:p";
            mockMvc.perform(get("/api/news-items/{guid}", guid).accept(MediaType.APPLICATION_XML))
                    .andExpect(status().isOk())
                    .andExpect(xpath("/n:newsItem/@guid", NAMESPACES).string(guid))
                    .andExpect(xpath("/n:newsItem/@version", NAMESPACES).string("2"))
                    .andExpect(xpath("/n:newsItem/@xml:lang", NAMESPACES).string("en"))
                    .andExpect(xpath("/n:newsItem/n:contentMeta/n:headline", NAMESPACES)
                            .string("Floods hit Cape Town, two dead"))
                    .andExpect(xpath(paragraphs, NAMESPACES).nodeCount(3))
                    .andExpect(xpath(paragraphs + "[1]", NAMESPACES).string("Rain fell."))
                    .andExpect(xpath(paragraphs + "[2]", NAMESPACES).string("Roads closed."))
                    .andExpect(xpath(paragraphs + "[3]", NAMESPACES).string("Schools shut."));
        }

        @Test
        @DisplayName("The one where the editor updates a GUID that was never issued. The result is \"not found\" and nothing is stored")
        void updatingANeverIssuedGuidIsNotFoundAndStoresNothing() throws Exception {
            String neverIssued = "urn:newsml:springbok.space:20261016:never-issued";

            mockMvc.perform(put("/api/news-items/{guid}", neverIssued)
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
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").value("news item not found"));

            mockMvc.perform(get("/api/news-items/{guid}", neverIssued).accept(MediaType.APPLICATION_XML))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Must require a headline, a body, a language and an author on every update")
    class MustRequireHeadlineBodyLanguageAndAuthorOnEveryUpdate {

        private static final Map<String, String> NAMESPACES = Map.of(
                "n", "http://iptc.org/std/nar/2006-10-01/");

        static Stream<Arguments> rejectedUpdates() {
            return Stream.of(
                    Arguments.of("headline is blank", "   ", "en", "Thandi Nkosi",
                            "headline", "headline required"),
                    Arguments.of("language is english", "Floods hit Cape Town, two dead", "english", "Thandi Nkosi",
                            "language", "language is not a valid language tag"),
                    Arguments.of("author is blank", "Floods hit Cape Town, two dead", "en", "   ",
                            "author", "author required"));
        }

        @Test
        @DisplayName("The one where the headline, body, language and author are all present, and the update becomes a new version")
        void completeUpdateBecomesANewVersion() throws Exception {
            String guid = createAndReadGuid();

            mockMvc.perform(put("/api/news-items/{guid}", guid)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson("Floods hit Cape Town, two dead", "en", "Thandi Nkosi")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.version").value(2));
        }

        @ParameterizedTest(name = "The one where the {0}, and the update is rejected with a reason and the item stays at version 1")
        @MethodSource("rejectedUpdates")
        @DisplayName("A rejected update creates no version")
        void incompleteUpdateIsRejectedAndCreatesNoVersion(String condition, String headline, String language,
                                                           String author, String expectedField,
                                                           String expectedDetail) throws Exception {
            String guid = createAndReadGuid();

            mockMvc.perform(put("/api/news-items/{guid}", guid)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson(headline, language, author)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.field").value(expectedField))
                    .andExpect(jsonPath("$.detail").value(expectedDetail));

            mockMvc.perform(get("/api/news-items/{guid}", guid).accept(MediaType.APPLICATION_XML))
                    .andExpect(status().isOk())
                    .andExpect(xpath("/n:newsItem/@version", NAMESPACES).string("1"))
                    .andExpect(xpath("/n:newsItem/n:contentMeta/n:headline", NAMESPACES)
                            .string("Floods hit Cape Town"));
        }

        private String updateJson(String headline, String language, String author) {
            return """
                    {
                      "headline": "%s",
                      "body": "Rain fell.\\n\\nRoads closed.\\n\\nSchools shut.",
                      "language": "%s",
                      "author": "%s",
                      "version": 1
                    }
                    """.formatted(headline, language, author);
        }

        private String createAndReadGuid() throws Exception {
            String response = mockMvc.perform(post("/api/news-items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "headline": "Floods hit Cape Town",
                                      "body": "Rain fell.\\n\\nRoads closed.\\n\\nSchools shut.",
                                      "language": "en"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            return JsonPath.read(response, "$.guid");
        }
    }
}
