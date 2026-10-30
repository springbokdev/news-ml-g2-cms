package space.springbok.example.newsitem.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Article content")
class ArticleContentTest {

    @Nested
    @DisplayName("Must require a headline, a body and a language to create an article")
    class MustRequireHeadlineBodyAndLanguage {

        static Stream<Arguments> missingRequiredFields() {
            return Stream.of(
                    Arguments.of("headline", "blank", "   ", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en"),
                    Arguments.of("body", "blank", "Floods hit Cape Town", "", "en"),
                    Arguments.of("language", "missing", "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", null));
        }

        @ParameterizedTest(name = "The one where the {0} is {1}, and the article is rejected: {0} required")
        @MethodSource("missingRequiredFields")
        void missingRequiredFieldIsRejected(String expectedField, String condition,
                                            String headline, String body, String language) {
            assertThatThrownBy(() -> ArticleContent.create(headline, body, language,
                    "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e"))
                    .isInstanceOf(MissingRequiredFieldException.class)
                    .extracting("field")
                    .isEqualTo(expectedField);
        }
    }

    @Nested
    @DisplayName("Must accept the language only as a well-formed BCP 47 tag")
    class MustAcceptLanguageOnlyAsWellFormedBcp47Tag {

        @ParameterizedTest(name = "The one where the language is {0}, and the article is rejected: not a valid language tag")
        @ValueSource(strings = {"english"})
        void malformedLanguageTagIsRejected(String language) {
            assertThatThrownBy(() -> ArticleContent.create(
                    "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", language,
                    "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e"))
                    .isInstanceOf(InvalidLanguageTagException.class)
                    .extracting("field")
                    .isEqualTo("language");
        }
    }

    @Nested
    @DisplayName("Must give every new news item a globally unique GUID")
    class MustGiveEveryNewNewsItemGloballyUniqueGuid {

        @Test
        @DisplayName("The one where an article is created on 16 Oct 2026 by provider springbok.space, and its GUID is urn:newsml:springbok.space:20261016:<unique-id>")
        void guidIsGeneratedWhenTheArticleIsCreated() {
            ArticleContent article = ArticleContent.create(
                    "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                    "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e");

            assertThat(article.guid().value()).isEqualTo("urn:newsml:springbok.space:20261016:3f2b9c1e");
        }
    }

    @Nested
    @DisplayName("Must give a newly created news item version 1")
    class MustGiveNewlyCreatedNewsItemVersionOne {

        @Test
        @DisplayName("The one where a new article is created, and its version is 1")
        void newArticleHasVersionOne() {
            ArticleContent article = ArticleContent.create(
                    "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                    "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e");

            assertThat(article.version()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Must save an accepted update as a new version of the same news item")
    class MustSaveAcceptedUpdateAsNewVersionOfSameNewsItem {

        @Test
        @DisplayName("The one where the item is at version 1 and the editor sends a new headline with the same body and en, and the item is now version 2 with the new headline and the same GUID")
        void acceptedUpdateIsTheNextVersionWithTheSameGuid() {
            ArticleContent version1 = ArticleContent.create(
                    "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                    "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e");

            ArticleContent version2 = version1.update(
                    "Floods hit Cape Town, two dead", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                    OffsetDateTime.parse("2026-10-17T08:15:00+02:00"));

            assertThat(version2.version()).isEqualTo(2);
            assertThat(version2.guid()).isEqualTo(version1.guid());
            assertThat(version2.headline()).isEqualTo("Floods hit Cape Town, two dead");
            assertThat(version2.body()).isEqualTo("Rain fell.\n\nRoads closed.\n\nSchools shut.");
            assertThat(version2.language()).isEqualTo("en");
            assertThat(version2.versionCreated()).isEqualTo(OffsetDateTime.parse("2026-10-17T08:15:00+02:00"));
        }
    }

    @Nested
    @DisplayName("Must populate the mandatory NewsML-G2 metadata on every news item")
    class MustPopulateMandatoryNewsMlG2Metadata {

        @Test
        @DisplayName("The one where an article is created at 2026-03-01T14:05:09-05:00, and its versionCreated is that instant with that offset")
        void versionCreatedIsTheCreationTimestamp() {
            ArticleContent article = ArticleContent.create(
                    "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                    "springbok.space", OffsetDateTime.parse("2026-03-01T14:05:09-05:00"), "3f2b9c1e");

            assertThat(article.versionCreated()).isEqualTo(OffsetDateTime.parse("2026-03-01T14:05:09-05:00"));
        }
    }
}
