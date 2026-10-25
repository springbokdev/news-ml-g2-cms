package space.springbok.example.newsitem.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import space.springbok.example.newsitem.domain.model.InvalidLanguageTagException;
import space.springbok.example.newsitem.domain.model.MalformedNewsItemGuidException;
import space.springbok.example.newsitem.domain.model.NewsItemGuid;
import space.springbok.example.newsitem.domain.model.NewsItemNotFoundException;

@WebMvcTest(WebExceptionHandlerTest.ThrowingController.class)
@DisplayName("Web exception handling")
class WebExceptionHandlerTest {

    @Autowired
    private MockMvcTester mvc;

    @Nested
    @DisplayName("Must answer a news item that does not exist with a 404 problem")
    class NotFound {

        @Test
        @DisplayName("The one where a partner asks for XML with a GUID that was never issued, and the response is still a 404 problem")
        void unknownGuidRequestedAsXmlIsNotFoundProblem() {
            assertThat(mvc.get().uri("/test/not-found").accept(MediaType.APPLICATION_XML))
                    .hasStatus(HttpStatus.NOT_FOUND)
                    .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .bodyJson()
                    .extractingPath("$.detail").isEqualTo("news item not found");
        }

        @Test
        @DisplayName("The one where the requested GUID is malformed, and the response is a 404 problem because no such item was ever issued")
        void malformedGuidIsNotFound() {
            assertThat(mvc.get().uri("/test/malformed-guid"))
                    .hasStatus(HttpStatus.NOT_FOUND)
                    .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .bodyJson()
                    .satisfies(json -> {
                        assertThat(json).extractingPath("$.detail").isEqualTo("news item not found");
                        assertThat(json).extractingPath("$.instance").isEqualTo("/test/malformed-guid");
                    });
        }
    }

    @Nested
    @DisplayName("Must answer a rejected article with a 400 problem naming the field")
    class InvalidArticleField {

        @Test
        @DisplayName("The one where the language is not a valid language tag, and the response is a 400 naming the language")
        void invalidLanguageTagIsBadRequestNamingTheField() {
            assertThat(mvc.get().uri("/test/invalid-language"))
                    .hasStatus(HttpStatus.BAD_REQUEST)
                    .bodyJson()
                    .extractingPath("$.field").isEqualTo("language");
        }
    }

    @Nested
    @DisplayName("Must keep the status of errors that Spring MVC itself raises")
    class FrameworkErrors {

        @Test
        @DisplayName("The one where the request body is not readable JSON, and the response is a 400 rather than a 500")
        void malformedJsonIsBadRequest() {
            assertThat(mvc.post().uri("/test/body")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{ \"headline\": \"Floods hit Cape Town\", "))
                    .hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("The one where a client accepts only JSON from an XML endpoint, and the response is a 406 rather than a 500")
        void clientAcceptingOnlyJsonIsNotAcceptable() {
            assertThat(mvc.get().uri("/test/xml-only").accept(MediaType.APPLICATION_JSON))
                    .hasStatus(HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @Nested
    @DisplayName("Must answer an unexpected failure with a 500 problem that leaks no internals")
    class UnexpectedFailure {

        @Test
        @DisplayName("The one where a GET fails unexpectedly, and the response is a 500 problem without the cause")
        void unexpectedFailureIsInternalServerErrorWithoutInternals() {
            assertThat(mvc.get().uri("/test/boom"))
                    .hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .bodyJson()
                    .satisfies(json -> {
                        assertThat(json).extractingPath("$.title").isEqualTo("Internal Server Error");
                        assertThat(json).extractingPath("$.status").isEqualTo(500);
                        assertThat(json).extractingPath("$.detail").isEqualTo("internal server error");
                    });
        }

        @Test
        @DisplayName("The one where a POST fails unexpectedly, and the response is a 500 problem for that request")
        void unexpectedFailureOnPostIsInternalServerError() {
            assertThat(mvc.post().uri("/test/boom")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .bodyJson()
                    .satisfies(json -> {
                        assertThat(json).extractingPath("$.detail").isEqualTo("internal server error");
                        assertThat(json).extractingPath("$.instance").isEqualTo("/test/boom");
                    });
        }
    }

    /**
     * Throws on demand, so the advice is exercised without any real endpoint.
     */
    @RestController
    static class ThrowingController {

        record Body(String headline) {
        }

        @GetMapping("/test/not-found")
        String notFound() {
            throw new NewsItemNotFoundException(new NewsItemGuid("urn:newsml:springbok.space:20261016:never-issued"));
        }

        @GetMapping("/test/malformed-guid")
        String malformedGuid() {
            throw new MalformedNewsItemGuidException();
        }

        @GetMapping("/test/invalid-language")
        String invalidLanguage() {
            throw new InvalidLanguageTagException("language");
        }

        @GetMapping(value = "/test/xml-only", produces = MediaType.APPLICATION_XML_VALUE)
        String xmlOnly() {
            return "<ok/>";
        }

        @PostMapping("/test/body")
        String body(@RequestBody Body body) {
            return body.headline();
        }

        @GetMapping("/test/boom")
        String boomOnGet() {
            throw new IllegalStateException("jdbc:postgresql://db.internal:5432 connection refused");
        }

        @PostMapping("/test/boom")
        String boomOnPost(@RequestBody Body body) {
            throw new IllegalStateException("jdbc:postgresql://db.internal:5432 connection refused");
        }
    }
}
