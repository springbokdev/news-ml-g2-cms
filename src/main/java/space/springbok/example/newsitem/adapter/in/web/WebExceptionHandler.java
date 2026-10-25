package space.springbok.example.newsitem.adapter.in.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import space.springbok.example.newsitem.domain.model.InvalidArticleFieldException;
import space.springbok.example.newsitem.domain.model.MalformedNewsItemGuidException;
import space.springbok.example.newsitem.domain.model.NewsItemNotFoundException;

/**
 * Central mapping of exceptions to HTTP problem responses.
 */
@Slf4j
@RestControllerAdvice
class WebExceptionHandler extends ResponseEntityExceptionHandler {

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

    @ExceptionHandler(Exception.class)
    ProblemDetail unexpectedFailure(Exception exception) {
        log.error("Unexpected failure while handling request", exception);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "internal server error");
    }
}
