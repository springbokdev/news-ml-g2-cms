package space.springbok.example.newsitem.domain.model;

/**
 * Raised when an update was not made against the latest version of a news item.
 */
public class VersionConflictException extends RuntimeException {

    public VersionConflictException() {
        super("update was not made against the latest version");
    }
}
