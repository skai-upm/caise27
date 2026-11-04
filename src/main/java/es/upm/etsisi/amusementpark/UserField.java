package es.upm.etsisi.amusementpark;

/**
 * Fields of a {@link User} that can be changed with {@code USER UPDATE}.
 * The DNI is intentionally not listed because it is immutable.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD
}
