package space.springbok.example.newsitem.adapter.in.web;

import jakarta.validation.Valid;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import space.springbok.example.newsitem.application.port.in.CreateNewsItemCommand;
import space.springbok.example.newsitem.application.port.in.CreateNewsItemUseCase;
import space.springbok.example.newsitem.application.port.in.GetNewsItemHistoryUseCase;
import space.springbok.example.newsitem.application.port.in.GetNewsItemUseCase;
import space.springbok.example.newsitem.application.port.in.GetNewsItemVersionUseCase;
import space.springbok.example.newsitem.application.port.in.UpdateNewsItemCommand;
import space.springbok.example.newsitem.application.port.in.UpdateNewsItemUseCase;
import space.springbok.example.newsitem.domain.model.ArticleContent;

@RestController
@RequiredArgsConstructor
class NewsItemController {

    private final CreateNewsItemUseCase createNewsItemUseCase;
    private final GetNewsItemUseCase getNewsItemUseCase;
    private final UpdateNewsItemUseCase updateNewsItemUseCase;
    private final GetNewsItemHistoryUseCase getNewsItemHistoryUseCase;
    private final GetNewsItemVersionUseCase getNewsItemVersionUseCase;
    private final CreateNewsItemResponseMapper responseMapper;
    private final UpdateNewsItemResponseMapper updateResponseMapper;
    private final NewsItemHistoryResponseMapper historyResponseMapper;
    private final NewsItemXmlRenderer xmlRenderer;

    @PostMapping("/api/news-items")
    ResponseEntity<CreateNewsItemResponse> createNewsItem(@Valid @RequestBody CreateNewsItemRequest request) {
        ArticleContent created = createNewsItemUseCase.createNewsItem(
                new CreateNewsItemCommand(request.headline(), request.body(), request.language(), request.author()));
        return ResponseEntity.status(HttpStatus.CREATED).body(responseMapper.toResponse(created));
    }

    @GetMapping(value = "/api/news-items/{guid}", produces = MediaType.APPLICATION_XML_VALUE)
    ResponseEntity<String> getNewsItem(@PathVariable String guid) {
        return ResponseEntity.ok(xmlRenderer.render(getNewsItemUseCase.getNewsItem(guid)));
    }

    @GetMapping(value = "/api/news-items/{guid}/versions/{version}", produces = MediaType.APPLICATION_XML_VALUE)
    ResponseEntity<String> getNewsItemVersion(@PathVariable String guid, @PathVariable int version) {
        return ResponseEntity.ok(xmlRenderer.render(getNewsItemVersionUseCase.getNewsItemVersion(guid, version)));
    }

    @GetMapping(value = "/api/news-items/{guid}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<NewsItemHistoryEntryResponse>> getNewsItemHistory(@PathVariable String guid) {
        return ResponseEntity.ok(historyResponseMapper.toHistory(getNewsItemHistoryUseCase.getNewsItemHistory(guid)));
    }

    @PutMapping("/api/news-items/{guid}")
    ResponseEntity<UpdateNewsItemResponse> updateNewsItem(@PathVariable String guid,
                                                          @Valid @RequestBody UpdateNewsItemRequest request) {
        ArticleContent updated = updateNewsItemUseCase.updateNewsItem(
                new UpdateNewsItemCommand(guid, request.headline(), request.body(), request.language(), request.author(),
                        request.version()));
        return ResponseEntity.ok(updateResponseMapper.toResponse(updated));
    }
}
