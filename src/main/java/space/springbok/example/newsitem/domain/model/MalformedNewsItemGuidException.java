package space.springbok.example.newsitem.domain.model;

/**
 * Raised when a value is not a well-formed news item GUID, so no item can ever have been issued with it.
 */
public class MalformedNewsItemGuidException extends IllegalArgumentException {

    public MalformedNewsItemGuidException() {
        super("not a well-formed news item GUID");
    }
}
