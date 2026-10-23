package space.springbok.example.newsitem.domain.model;

/**
 * Raised when a field of the article content breaks a business rule.
 */
public abstract class InvalidArticleFieldException extends RuntimeException {

    private final String field;

    protected InvalidArticleFieldException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String field() {
        return field;
    }
}
