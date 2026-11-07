package space.springbok.example.newsitem.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.springbok.example.newsitem.application.port.out.LoadNewsItemHistoryPort;
import space.springbok.example.newsitem.domain.model.NewsItemGuid;
import space.springbok.example.newsitem.domain.model.NewsItemNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Retrieving the modification history of a news item")
class GetNewsItemHistoryServiceTest {

    @Mock
    private LoadNewsItemHistoryPort loadNewsItemHistoryPort;

    @Test
    @DisplayName("The one where the history of a GUID that was never issued is requested, and the result is \"not found\"")
    void historyOfANeverIssuedGuidIsNotFound() {
        NewsItemGuid neverIssued = new NewsItemGuid("urn:newsml:springbok.space:20261016:never-issued");
        given(loadNewsItemHistoryPort.findAllVersionsByGuid(neverIssued)).willReturn(List.of());
        GetNewsItemHistoryService service = new GetNewsItemHistoryService(loadNewsItemHistoryPort);

        assertThatThrownBy(() -> service.getNewsItemHistory(neverIssued.value()))
                .isInstanceOf(NewsItemNotFoundException.class)
                .extracting("guid")
                .isEqualTo(neverIssued);
    }
}
