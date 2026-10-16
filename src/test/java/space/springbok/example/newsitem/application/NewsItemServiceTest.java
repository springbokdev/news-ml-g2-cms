package space.springbok.example.newsitem.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.springbok.example.newsitem.application.port.in.CreateNewsItemCommand;
import space.springbok.example.newsitem.application.port.out.SaveArticleContentPort;
import space.springbok.example.newsitem.domain.model.ArticleContent;
import space.springbok.example.newsitem.domain.model.MissingRequiredFieldException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Creating a news item")
class NewsItemServiceTest {

    @Mock
    private SaveArticleContentPort saveArticleContentPort;

    @Test
    @DisplayName("The one where a valid article is created, and its content is saved")
    void validArticleContentIsSaved() {
        NewsItemService service = new NewsItemService(saveArticleContentPort);

        service.createNewsItem(new CreateNewsItemCommand(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en"));

        then(saveArticleContentPort).should().save(new ArticleContent(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en"));
    }

    @Test
    @DisplayName("The one where the headline is blank, and creating the news item is rejected: headline required")
    void blankHeadlineIsRejectedOnCreate() {
        NewsItemService service = new NewsItemService(saveArticleContentPort);
        CreateNewsItemCommand command = new CreateNewsItemCommand(
                "   ", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en");

        assertThatThrownBy(() -> service.createNewsItem(command))
                .isInstanceOf(MissingRequiredFieldException.class)
                .extracting("field")
                .isEqualTo("headline");
        then(saveArticleContentPort).should(never()).save(any());
    }
}
