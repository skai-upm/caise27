package es.upm.amusementpark.business.model;

/**
 * User fields that can be changed with USER UPDATE. DNI is deliberately absent.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD;

    /**
     * Returns the field with the given name, or null if it is not editable.
     */
    public static UserField fromName(String text) {
        for (UserField field : values()) {
            if (field.name().equals(text)) {
                return field;
            }
        }
        return null;
    }
}
