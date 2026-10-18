package space.springbok.example.newsitem.domain.model;

import java.util.regex.Pattern;

/**
 * The journalist's article content: what a news item carries before the system
 * gives it an identity.
 */
public record ArticleContent(String headline, String body, String language) {

    private static final Pattern BCP_47_TAG = Pattern.compile(
            "^[A-Za-z]{2,3}(-[A-Za-z]{4})?(-([A-Za-z]{2}|[0-9]{3}))?(-([A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*$");

    public ArticleContent {
        requireText(headline, "headline");
        requireText(body, "body");
        requireText(language, "language");
        requireWellFormedLanguageTag(language);
    }

    private static void requireWellFormedLanguageTag(String language) {
        if (!BCP_47_TAG.matcher(language).matches()) {
            throw new InvalidLanguageTagException("language");
        }
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new MissingRequiredFieldException(field);
        }
    }
}
