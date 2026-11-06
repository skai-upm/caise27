package es.upm.park.business.model;

/**
 * User fields that may be modified after creation. The DNI is deliberately
 * absent: it is immutable.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD;

    /**
     * Returns the field with exactly this name, or {@code null} if the name
     * does not denote a modifiable field.
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
