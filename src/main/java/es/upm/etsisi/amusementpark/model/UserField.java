package es.upm.etsisi.amusementpark.model;

/**
 * User attributes that may be changed with USER UPDATE. DNI is deliberately absent.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD;

    /**
     * Returns the field with exactly this name, or null if there is none.
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
