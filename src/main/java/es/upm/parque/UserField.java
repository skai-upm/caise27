package es.upm.parque;

/**
 * Fields of a user that can be changed with USER UPDATE. The DNI is deliberately absent.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD;

    /**
     * Returns the field with the given name (case-insensitive), or {@code null} if there is none.
     */
    public static UserField parse(String text) {
        for (UserField field : values()) {
            if (field.name().equalsIgnoreCase(text.trim())) {
                return field;
            }
        }
        return null;
    }
}
