package es.upm.amusementpark.business.model;

/**
 * User attributes that may be modified with USER UPDATE. The DNI is deliberately absent.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD;

    /** Returns the field with exactly this name, or null if it is not modifiable. */
    public static UserField fromText(String text) {
        for (UserField field : values()) {
            if (field.name().equals(text)) {
                return field;
            }
        }
        return null;
    }
}
