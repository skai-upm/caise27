package es.upm.amusementpark.business.model;

/**
 * Fields of a {@link User} that can be changed with {@code USER UPDATE}.
 * The DNI is deliberately absent because it cannot be changed.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD;

    /** Returns the field with exactly the given name, or {@code null} if there is none. */
    public static UserField fromName(String text) {
        for (UserField field : values()) {
            if (field.name().equals(text)) {
                return field;
            }
        }
        return null;
    }
}
