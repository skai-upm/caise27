package es.upm.parque;

/**
 * User fields that can be changed through USER UPDATE. The DNI is deliberately absent.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD;

    /**
     * Case-insensitive lookup.
     *
     * @throws IllegalArgumentException if the text is not an editable field
     */
    public static UserField fromText(String text) {
        for (UserField field : values()) {
            if (field.name().equalsIgnoreCase(text.trim())) {
                return field;
            }
        }
        throw new IllegalArgumentException("Field not editable: " + text);
    }
}
