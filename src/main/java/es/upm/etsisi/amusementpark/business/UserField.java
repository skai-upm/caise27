package es.upm.etsisi.amusementpark.business;

/**
 * User fields that USER UPDATE may modify. DNI is deliberately absent.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD;

    /** Returns the field with the given name, or null if it is not modifiable. */
    public static UserField fromName(String name) {
        for (UserField field : values()) {
            if (field.name().equals(name)) {
                return field;
            }
        }
        return null;
    }
}
