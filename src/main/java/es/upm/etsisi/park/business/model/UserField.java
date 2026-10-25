package es.upm.etsisi.park.business.model;

import java.util.Locale;

/**
 * User attributes that may be changed with USER UPDATE. The DNI is intentionally absent.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD;

    /**
     * Resolves a field name case-insensitively.
     *
     * @return the matching field, or {@code null} if the name is not a modifiable field
     */
    public static UserField fromName(String name) {
        if (name == null) {
            return null;
        }
        String normalized = name.trim().toUpperCase(Locale.ROOT);
        for (UserField field : values()) {
            if (field.name().equals(normalized)) {
                return field;
            }
        }
        return null;
    }
}
