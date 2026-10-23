package space.springbok.example.newsitem.adapter.in.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import space.springbok.example.newsitem.application.port.in.CreateNewsItemCommand;
import space.springbok.example.newsitem.application.port.in.CreateNewsItemUseCase;
import space.springbok.example.newsitem.application.port.in.GetNewsItemUseCase;
import space.springbok.example.newsitem.domain.model.InvalidArticleFieldException;
import space.springbok.example.newsitem.domain.model.MalformedNewsItemGuidException;
import space.springbok.example.newsitem.domain.model.NewsItemGuid;
import space.springbok.example.newsitem.domain.model.NewsItemNotFoundException;

@RestController
class NewsItemController {

    private final CreateNewsItemUseCase createNewsItemUseCase;
    private final GetNewsItemUseCase getNewsItemUseCase;
    private final CreateNewsItemResponseMapper responseMapper;
    private final NewsItemXmlRenderer xmlRenderer;

    NewsItemController(CreateNewsItemUseCase createNewsItemUseCase, GetNewsItemUseCase getNewsItemUseCase,
                       CreateNewsItemResponseMapper responseMapper, NewsItemXmlRenderer xmlRenderer) {
        this.createNewsItemUseCase = createNewsItemUseCase;
        this.getNewsItemUseCase = getNewsItemUseCase;
        this.responseMapper = responseMapper;
        this.xmlRenderer = xmlRenderer;
    }

    @PostMapping("/api/news-items")
    @ResponseStatus(HttpStatus.CREATED)
    CreateNewsItemResponse createNewsItem(@Valid @RequestBody CreateNewsItemRequest request) {
        NewsItemGuid guid = createNewsItemUseCase.createNewsItem(
                new CreateNewsItemCommand(request.headline(), request.body(), request.language()));
        return responseMapper.toResponse(guid);
    }

    @GetMapping(value = "/api/news-items/{guid}", produces = MediaType.APPLICATION_XML_VALUE)
    String getNewsItem(@PathVariable String guid) {
        return xmlRenderer.render(getNewsItemUseCase.getNewsItem(new NewsItemGuid(guid)));
    }

    @ExceptionHandler({NewsItemNotFoundException.class, MalformedNewsItemGuidException.class})
    ProblemDetail newsItemNotFound() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "news item not found");
    }

    @ExceptionHandler(InvalidArticleFieldException.class)
    ProblemDetail invalidArticleField(InvalidArticleFieldException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setProperty("field", exception.field());
        return problem;
    }

}
