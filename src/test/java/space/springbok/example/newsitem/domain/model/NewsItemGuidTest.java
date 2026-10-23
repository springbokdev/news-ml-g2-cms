package space.springbok.example.newsitem.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("A news item GUID")
class NewsItemGuidTest {

    @Test
    @DisplayName("The one where the GUID is made from the provider, the creation date and a unique id")
    void guidIsMadeFromProviderCreationDateAndUniqueId() {
        NewsItemGuid guid = NewsItemGuid.create("springbok.space", LocalDate.of(2026, 10, 16), "3f2b9c1e");

        assertThat(guid.value()).isEqualTo("urn:newsml:springbok.space:20261016:3f2b9c1e");
    }

    @ParameterizedTest(name = "The one where the GUID is [{0}], and it is rejected as not well-formed")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "not-a-urn", "urn:newsml:springbok.space:2026-10-16:3f2b9c1e",
            "urn:newsml::20261016:3f2b9c1e", "urn:newsml:springbok.space:20261016:"})
    void malformedGuidIsRejected(String value) {
        assertThatThrownBy(() -> new NewsItemGuid(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("not a well-formed news item GUID");
    }
}
