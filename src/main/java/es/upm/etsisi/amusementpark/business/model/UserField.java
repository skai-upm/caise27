package es.upm.etsisi.amusementpark.business.model;

import java.util.Locale;

/** Fields of a user that can be changed through USER UPDATE. The DNI is intentionally absent. */
public enum UserField {
    NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD;

    /**
     * Parses a field name from a command token (case-insensitive, surrounding blanks ignored).
     *
     * @return the field, or {@code null} if the token does not name an editable field
     */
    public static UserField parse(String token) {
        if (token == null) {
            return null;
        }
        String wanted = token.trim().toUpperCase(Locale.ROOT);
        for (UserField field : values()) {
            if (field.name().equals(wanted)) {
                return field;
            }
        }
        return null;
    }
}
