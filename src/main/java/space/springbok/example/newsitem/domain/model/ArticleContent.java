package space.springbok.example.newsitem.domain.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The journalist's article content, and the aggregate root of a news item: it owns the
 * GUID the system gives the item when it is created.
 */
public record ArticleContent(NewsItemGuid guid, String headline, String body, String language) {

    private static final int INITIAL_VERSION = 1;

    private static final Pattern BCP_47_TAG = Pattern.compile(
            "^[A-Za-z]{2,3}(-[A-Za-z]{4})?(-([A-Za-z]{2}|[0-9]{3}))?(-([A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*$");

    public ArticleContent {
        Objects.requireNonNull(guid, "guid");
        requireText(headline, "headline");
        requireText(body, "body");
        requireText(language, "language");
        requireWellFormedLanguageTag(language);
    }

    public static ArticleContent create(String headline, String body, String language,
                                        String provider, LocalDate creationDate, String uniqueId) {
        return new ArticleContent(
                NewsItemGuid.create(provider, creationDate, uniqueId), headline, body, language);
    }

    public int version() {
        return INITIAL_VERSION;
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
