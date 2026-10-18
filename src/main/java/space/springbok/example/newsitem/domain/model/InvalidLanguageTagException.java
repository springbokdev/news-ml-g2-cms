package space.springbok.example.newsitem.domain.model;

/**
 * Raised when the article's language is not a well-formed BCP 47 tag.
 */
public class InvalidLanguageTagException extends InvalidArticleFieldException {

    public InvalidLanguageTagException(String field) {
        super(field, field + " is not a valid language tag");
    }
}
