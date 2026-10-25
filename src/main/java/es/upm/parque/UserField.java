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
     * Returns the field matching the given command value, or null if it is not editable.
     */
    public static UserField fromString(String value) {
        for (UserField field : values()) {
            if (field.name().equalsIgnoreCase(value.trim())) {
                return field;
            }
        }
        return null;
    }
}
