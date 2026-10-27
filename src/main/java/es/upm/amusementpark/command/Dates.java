package es.upm.amusementpark.command;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Parsing of command dates in the yyyy-MM-dd format.
 */
final class Dates {

    private Dates() {
    }

    /** Returns the date, or null if the text is not a valid yyyy-MM-dd date. */
    static LocalDate parse(String text) {
        if (text.length() != 10) {
            return null;
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
