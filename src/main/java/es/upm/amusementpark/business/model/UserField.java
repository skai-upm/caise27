package es.upm.amusementpark.business.model;

/**
 * User attributes that can be changed with USER UPDATE. The DNI is intentionally absent.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD;

    /**
     * Returns the field with exactly the given name, or {@code null} if there is none.
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
