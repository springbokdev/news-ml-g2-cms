package space.springbok.example.newsitem.domain.model;

/**
 * Raised when article content lacks a field the business requires.
 */
public class MissingRequiredFieldException extends RuntimeException {

    private final String field;

    public MissingRequiredFieldException(String field) {
        super(field + " required");
        this.field = field;
    }

    public String field() {
        return field;
    }
}
