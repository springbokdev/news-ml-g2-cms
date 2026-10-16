package space.springbok.example.newsitem.domain.model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Article content must have a headline, a body and a language")
class ArticleContentTest {

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
        assertThatThrownBy(() -> new ArticleContent(headline, body, language))
                .isInstanceOf(MissingRequiredFieldException.class)
                .extracting("field")
                .isEqualTo(expectedField);
    }
}
