package es.upm.amusementpark;

/**
 * User attributes that can be changed with {@code USER UPDATE}. The DNI is
 * deliberately absent because it cannot be modified.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD;

    /**
     * Returns the field with exactly the given name, or {@code null} if there
     * is none.
     */
    public static UserField fromName(String name) {
        for (UserField field : values()) {
            if (field.name().equals(name)) {
                return field;
            }
        }
        return null;
    }
}
