package space.springbok.example.newsitem.domain.model;

/**
 * Raised when article content lacks a field the business requires.
 */
public class MissingRequiredFieldException extends InvalidArticleFieldException {

    public MissingRequiredFieldException(String field) {
        super(field, field + " required");
    }
}
