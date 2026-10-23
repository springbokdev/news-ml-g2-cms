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

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import java.io.StringReader;
import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.hamcrest.Matchers.matchesPattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

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

    @Nested
    @Import(FixedClockConfig.class)
    @DisplayName("Must populate the mandatory NewsML-G2 metadata on every news item")
    class MustPopulateMandatoryNewsMlG2Metadata {

        private static final Map<String, String> NAMESPACES = Map.of("n", "http://iptc.org/std/nar/2006-10-01/");

        static Stream<Arguments> mandatoryMetadata() {
            return Stream.of(
                    Arguments.of("standard", "/n:newsItem/@standard", "NewsML-G2"),
                    Arguments.of("standardversion", "/n:newsItem/@standardversion", "2.34"),
                    Arguments.of("catalogRef", "/n:newsItem/n:catalogRef/@href",
                            "http://www.iptc.org/std/catalog/catalog.IPTC-G2-Standards_38.xml"),
                    Arguments.of("xml:lang", "/n:newsItem/@xml:lang", "en"),
                    Arguments.of("itemMeta/itemClass", "/n:newsItem/n:itemMeta/n:itemClass/@qcode", "ninat:text"),
                    Arguments.of("itemMeta/provider", "/n:newsItem/n:itemMeta/n:provider/@literal", "springbok.space"),
                    Arguments.of("itemMeta/versionCreated", "/n:newsItem/n:itemMeta/n:versionCreated",
                            "2026-10-16T09:30:00+02:00"),
                    Arguments.of("itemMeta/pubStatus", "/n:newsItem/n:itemMeta/n:pubStatus/@qcode", "stat:usable"),
                    Arguments.of("contentMeta/headline", "/n:newsItem/n:contentMeta/n:headline", "Floods hit Cape Town"));
        }

        @ParameterizedTest(name = "The one where an article is created, and its {0} is {2}")
        @DisplayName("The mandatory metadata is populated on the news item")
        @MethodSource("mandatoryMetadata")
        void mandatoryMetadataIsPopulated(String element, String xpathExpression, String expectedValue) throws Exception {
            String guid = createAndReadGuid("""
                    {
                      "headline": "Floods hit Cape Town",
                      "body": "Rain fell.\\n\\nRoads closed.\\n\\nSchools shut.",
                      "language": "en"
                    }
                    """);

            mockMvc.perform(get("/api/news-items/{guid}", guid).accept(MediaType.APPLICATION_XML))
                    .andExpect(status().isOk())
                    .andExpect(xpath(xpathExpression, NAMESPACES).string(expectedValue));
        }

        @Test
        @DisplayName("The one where the journalist supplies a provider, item class and publication status, and the system ignores them")
        void journalistCannotSetProviderItemClassOrPubStatus() throws Exception {
            String guid = createAndReadGuid("""
                    {
                      "provider": "example.org",
                      "itemClass": "ninat:image",
                      "pubStatus": "stat:withheld",
                      "headline": "Floods hit Cape Town",
                      "body": "Rain fell.\\n\\nRoads closed.\\n\\nSchools shut.",
                      "language": "en"
                    }
                    """);

            mockMvc.perform(get("/api/news-items/{guid}", guid).accept(MediaType.APPLICATION_XML))
                    .andExpect(status().isOk())
                    .andExpect(xpath("/n:newsItem/n:itemMeta/n:provider/@literal", NAMESPACES).string("springbok.space"))
                    .andExpect(xpath("/n:newsItem/n:itemMeta/n:itemClass/@qcode", NAMESPACES).string("ninat:text"))
                    .andExpect(xpath("/n:newsItem/n:itemMeta/n:pubStatus/@qcode", NAMESPACES).string("stat:usable"));
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
    @DisplayName("Must carry the body as XHTML paragraphs")
    class MustCarryTheBodyAsXhtmlParagraphs {

        private static final Map<String, String> NAMESPACES = Map.of(
                "n", "http://iptc.org/std/nar/2006-10-01/",
                "x", "http://www.w3.org/1999/xhtml");

        private static final String PARAGRAPHS = "/n:newsItem/n:contentSet/n:inlineXML[@contenttype='application/xhtml+xml']"
                + "/x:html/x:body/x:p";

        @Test
        @DisplayName("The one where the body is \"Rain fell.⏎⏎Roads closed.⏎⏎Schools shut.\", and it renders as three <p> elements in order")
        void threeParagraphsRenderAsThreeParagraphElementsInOrder() throws Exception {
            String guid = createAndReadGuid("""
                    {
                      "headline": "Floods hit Cape Town",
                      "body": "Rain fell.\\n\\nRoads closed.\\n\\nSchools shut.",
                      "language": "en"
                    }
                    """);

            mockMvc.perform(get("/api/news-items/{guid}", guid).accept(MediaType.APPLICATION_XML))
                    .andExpect(status().isOk())
                    .andExpect(xpath(PARAGRAPHS, NAMESPACES).nodeCount(3))
                    .andExpect(xpath(PARAGRAPHS + "[1]", NAMESPACES).string("Rain fell."))
                    .andExpect(xpath(PARAGRAPHS + "[2]", NAMESPACES).string("Roads closed."))
                    .andExpect(xpath(PARAGRAPHS + "[3]", NAMESPACES).string("Schools shut."));
        }

        @Test
        @DisplayName("The one where the body is a single paragraph with no blank lines, and it renders as exactly one <p>")
        void singleParagraphRendersAsExactlyOneParagraphElement() throws Exception {
            String guid = createAndReadGuid("""
                    {
                      "headline": "Floods hit Cape Town",
                      "body": "Rain fell.",
                      "language": "en"
                    }
                    """);

            mockMvc.perform(get("/api/news-items/{guid}", guid).accept(MediaType.APPLICATION_XML))
                    .andExpect(status().isOk())
                    .andExpect(xpath(PARAGRAPHS, NAMESPACES).nodeCount(1))
                    .andExpect(xpath(PARAGRAPHS + "[1]", NAMESPACES).string("Rain fell."));
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
    @DisplayName("Must produce News Item XML that validates against the NewsML-G2 2.34 schema")
    class MustProduceNewsItemXmlThatValidatesAgainstTheSchema {

        private static final Map<String, String> NAMESPACES = Map.of("n", "http://iptc.org/std/nar/2006-10-01/");

        @Test
        @DisplayName("The one where a created article's XML passes validation against the IPTC NewsML-G2 2.34 XSD")
        void createdArticleXmlPassesSchemaValidation() throws Exception {
            String guid = createAndReadGuid("""
                    {
                      "headline": "Floods hit Cape Town",
                      "body": "Rain fell.\\n\\nRoads closed.\\n\\nSchools shut.",
                      "language": "en"
                    }
                    """);

            String xml = fetchXml(guid);

            assertThatCode(() -> newsMlG2Schema().newValidator().validate(new StreamSource(new StringReader(xml))))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("The one where the headline is `Q&A: <Budget> 2026`, and the characters are escaped so the XML stays valid and the headline reads exactly as entered")
        void specialCharactersInHeadlineAreEscapedAndReadBackExactly() throws Exception {
            String guid = createAndReadGuid("""
                    {
                      "headline": "Q&A: <Budget> 2026",
                      "body": "Rain fell.\\n\\nRoads closed.\\n\\nSchools shut.",
                      "language": "en"
                    }
                    """);

            String xml = fetchXml(guid);

            assertThatCode(() -> newsMlG2Schema().newValidator().validate(new StreamSource(new StringReader(xml))))
                    .doesNotThrowAnyException();
            mockMvc.perform(get("/api/news-items/{guid}", guid).accept(MediaType.APPLICATION_XML))
                    .andExpect(status().isOk())
                    .andExpect(xpath("/n:newsItem/n:contentMeta/n:headline", NAMESPACES).string("Q&A: <Budget> 2026"));
        }

        private Schema newsMlG2Schema() throws Exception {
            URL xsd = getClass().getClassLoader().getResource("xsd/NewsML-G2_2.34-spec-All-Power.xsd");
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "file,jar");
            return factory.newSchema(xsd);
        }

        private String fetchXml(String guid) throws Exception {
            return mockMvc.perform(get("/api/news-items/{guid}", guid).accept(MediaType.APPLICATION_XML))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
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
    @DisplayName("Must let partners retrieve the News Item XML by its GUID")
    class MustLetPartnersRetrieveTheNewsItemXmlByItsGuid {

        private static final Map<String, String> NAMESPACES = Map.of("n", "http://iptc.org/std/nar/2006-10-01/");

        @Test
        @DisplayName("The one where an item is created, and fetching it by its GUID returns its <newsItem> XML at version 1")
        void createdItemIsFetchedByItsGuidAtVersionOne() throws Exception {
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

            mockMvc.perform(get("/api/news-items/{guid}", guid).accept(MediaType.APPLICATION_XML))
                    .andExpect(status().isOk())
                    .andExpect(xpath("/n:newsItem/@guid", NAMESPACES).string(guid))
                    .andExpect(xpath("/n:newsItem/@version", NAMESPACES).string("1"));
        }

        @Test
        @DisplayName("The one where a GUID that was never issued is requested, and the result is \"not found\"")
        void neverIssuedGuidIsNotFound() throws Exception {
            mockMvc.perform(get("/api/news-items/{guid}", "urn:newsml:springbok.space:20261016:never-issued")
                            .accept(MediaType.APPLICATION_XML))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").value("news item not found"));
        }
    }
}
