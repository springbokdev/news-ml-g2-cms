package space.springbok.example.newsitem.adapter.in.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import space.springbok.example.newsitem.application.port.in.CreateNewsItemCommand;
import space.springbok.example.newsitem.application.port.in.CreateNewsItemUseCase;
import space.springbok.example.newsitem.domain.model.InvalidArticleFieldException;
import space.springbok.example.newsitem.domain.model.NewsItemGuid;

@RestController
class NewsItemController {

    private final CreateNewsItemUseCase createNewsItemUseCase;
    private final CreateNewsItemResponseMapper responseMapper;

    NewsItemController(CreateNewsItemUseCase createNewsItemUseCase, CreateNewsItemResponseMapper responseMapper) {
        this.createNewsItemUseCase = createNewsItemUseCase;
        this.responseMapper = responseMapper;
    }

    @PostMapping("/api/news-items")
    @ResponseStatus(HttpStatus.CREATED)
    CreateNewsItemResponse createNewsItem(@Valid @RequestBody CreateNewsItemRequest request) {
        NewsItemGuid guid = createNewsItemUseCase.createNewsItem(
                new CreateNewsItemCommand(request.headline(), request.body(), request.language()));
        return responseMapper.toResponse(guid);
    }

    @ExceptionHandler(InvalidArticleFieldException.class)
    ProblemDetail invalidArticleField(InvalidArticleFieldException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setProperty("field", exception.field());
        return problem;
    }

}
