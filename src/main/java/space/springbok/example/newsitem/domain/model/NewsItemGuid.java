package space.springbok.example.newsitem.domain.model;

import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * The globally unique identity of a news item:
 * {@code urn:newsml:<provider>:<yyyyMMdd>:<unique-id>}.
 */
public record NewsItemGuid(String value) {

    private static final Pattern FORMAT = Pattern.compile("^urn:newsml:[^:]+:[0-9]{8}:[^:]+$");

    public NewsItemGuid {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("not a well-formed news item GUID");
        }
    }

    public static NewsItemGuid create(String provider, LocalDate creationDate, String uniqueId) {
        return new NewsItemGuid("urn:newsml:" + provider + ":" + creationDate.format(BASIC_ISO_DATE) + ":" + uniqueId);
    }
}
