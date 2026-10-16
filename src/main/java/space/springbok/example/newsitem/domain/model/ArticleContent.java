package space.springbok.example.newsitem.domain.model;

/**
 * The journalist's article content: what a news item carries before the system
 * gives it an identity.
 */
public record ArticleContent(String headline, String body, String language) {

    public ArticleContent {
        requireText(headline, "headline");
        requireText(body, "body");
        requireText(language, "language");
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new MissingRequiredFieldException(field);
        }
    }
}
