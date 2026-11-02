package com.amusementpark.business.model;

/**
 * User fields that can be changed with USER UPDATE. The DNI is deliberately absent.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD;

    /**
     * Resolves a field name from command text, ignoring case and surrounding spaces.
     *
     * @return the matching field, or {@code null} if the name is not updatable
     */
    public static UserField fromText(String text) {
        String wanted = text.trim().toUpperCase();
        for (UserField field : values()) {
            if (field.name().equals(wanted)) {
                return field;
            }
        }
        return null;
    }
}
