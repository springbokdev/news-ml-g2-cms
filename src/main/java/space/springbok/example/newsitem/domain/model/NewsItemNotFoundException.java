package space.springbok.example.newsitem.domain.model;

/**
 * Raised when no news item exists for a GUID.
 */
public class NewsItemNotFoundException extends RuntimeException {

    private final NewsItemGuid guid;

    public NewsItemNotFoundException(NewsItemGuid guid) {
        super("news item " + guid.value() + " not found");
        this.guid = guid;
    }

    public NewsItemGuid guid() {
        return guid;
    }
}
