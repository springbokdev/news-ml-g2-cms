package space.springbok.example.newsitem.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.springbok.example.newsitem.application.port.out.LoadArticleContentPort;
import space.springbok.example.newsitem.domain.model.NewsItemGuid;
import space.springbok.example.newsitem.domain.model.NewsItemNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Retrieving one version of a news item")
class GetNewsItemVersionServiceTest {

    @Mock
    private LoadArticleContentPort loadArticleContentPort;

    @Test
    @DisplayName("The one where version 3 of an item at version 2 is requested, and the result is \"not found\"")
    void aVersionThatWasNeverCreatedIsNotFound() {
        NewsItemGuid guid = new NewsItemGuid("urn:newsml:springbok.space:20261016:3f2b9c1e");
        given(loadArticleContentPort.findByGuidAndVersion(guid, 3)).willReturn(Optional.empty());
        GetNewsItemVersionService service = new GetNewsItemVersionService(loadArticleContentPort);

        assertThatThrownBy(() -> service.getNewsItemVersion(guid.value(), 3))
                .isInstanceOf(NewsItemNotFoundException.class)
                .extracting("guid")
                .isEqualTo(guid);
    }
}
