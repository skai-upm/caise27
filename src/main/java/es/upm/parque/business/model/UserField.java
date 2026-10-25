package es.upm.parque.business.model;

/**
 * User fields that can be changed with USER UPDATE. The DNI is deliberately absent.
 */
public enum UserField {
    NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD;

    /**
     * Returns the field with the given name ignoring case, or null if it is not editable.
     */
    public static UserField parse(String text) {
        for (UserField field : values()) {
            if (field.name().equalsIgnoreCase(text.trim())) {
                return field;
            }
        }
        return null;
    }
}
