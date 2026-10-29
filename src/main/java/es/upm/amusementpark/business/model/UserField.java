package es.upm.amusementpark.business.model;

import es.upm.amusementpark.business.exception.InvalidValueException;

/** User fields that may be modified. The DNI is deliberately not one of them. */
public enum UserField {
    NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD;

    /** @throws InvalidValueException for a name that is not a modifiable field */
    public static UserField parse(String text) {
        String value = text == null ? "" : text.trim();
        for (UserField field : values()) {
            if (field.name().equals(value)) {
                return field;
            }
        }
        throw new InvalidValueException("unknown or non-modifiable field (expected NAME, SURNAME, EMAIL, PASSWORD,"
                + " PHONE or CARD): " + text);
    }
}
