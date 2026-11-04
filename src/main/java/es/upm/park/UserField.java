package es.upm.park;

/**
 * Fields of a {@link User} that can be changed through {@code USER UPDATE}.
 * The DNI is intentionally absent because it is immutable.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD
}
