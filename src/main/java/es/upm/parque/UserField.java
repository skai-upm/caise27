package es.upm.parque;

/**
 * User fields that can be changed with USER UPDATE. The DNI is intentionally absent.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD;

    /**
     * Returns the field with the given name (case-insensitive), or null if there is none.
     */
    public static UserField fromName(String text) {
        for (UserField field : values()) {
            if (field.name().equalsIgnoreCase(text.trim())) {
                return field;
            }
        }
        return null;
    }
}
