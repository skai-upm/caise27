package es.upm.etsisi.amusementpark.business.model;

import es.upm.etsisi.amusementpark.business.exception.InvalidValueException;

/**
 * User fields that may be modified. The DNI is deliberately not included.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD;

    /**
     * @throws InvalidValueException if {@code text} is not a modifiable field
     */
    public static UserField parse(String text) {
        for (UserField field : values()) {
            if (field.name().equals(text)) {
                return field;
            }
        }
        throw new InvalidValueException("Invalid field, expected NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD: "
                + text);
    }
}
