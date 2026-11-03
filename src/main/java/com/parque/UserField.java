package com.parque;

/**
 * Fields of a {@link User} that can be changed through {@code USER UPDATE}.
 * The DNI is intentionally absent: it cannot be modified after creation.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD;

    /**
     * Returns the field with exactly the given name, or {@code null} if none matches.
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
