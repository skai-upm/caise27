package es.upm.amusementpark.business.model;

/**
 * User fields that can be changed with USER UPDATE. The DNI is not included
 * because it cannot be modified.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD;

    /**
     * Returns the field with exactly the given name, or {@code null} if the
     * name is not a modifiable field.
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
