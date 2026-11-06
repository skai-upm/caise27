package es.upm.park.business.model;

import es.upm.park.business.exception.InvalidInputException;

/** The user fields that USER UPDATE may modify. The DNI is deliberately absent. */
public enum UserField {
    NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD;

    public static UserField parse(String text) {
        if ("DNI".equals(text)) {
            throw new InvalidInputException("The DNI cannot be modified");
        }
        for (UserField field : values()) {
            if (field.name().equals(text)) {
                return field;
            }
        }
        throw new InvalidInputException("Unknown user field: " + text
                + " (expected NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD)");
    }
}
