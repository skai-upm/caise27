package es.upm.amusementpark.business;

/**
 * User attributes that may be modified with {@code USER UPDATE}. The DNI is deliberately absent.
 */
public enum UserField {
    NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD;

    /** @return the field with that exact name, or {@code null} */
    public static UserField parse(String text) {
        for (UserField field : values()) {
            if (field.name().equals(text)) {
                return field;
            }
        }
        return null;
    }
}
