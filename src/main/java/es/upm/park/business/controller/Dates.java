package es.upm.park.business.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

import es.upm.park.business.service.ParkException;

/**
 * Strict parsing of {@code yyyy-MM-dd} dates used by commands.
 */
public final class Dates {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private Dates() {
    }

    /**
     * @throws ParkException when the text is not a valid date
     */
    public static LocalDate parse(String text) {
        if (text == null) {
            throw new ParkException("invalid date null");
        }
        try {
            return LocalDate.parse(text, FORMAT);
        } catch (DateTimeParseException e) {
            throw new ParkException("invalid date " + text);
        }
    }
}
