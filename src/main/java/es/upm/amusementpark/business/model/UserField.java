package es.upm.amusementpark.business.model;

/**
 * User attributes that may be modified with USER UPDATE. The DNI is
 * intentionally absent because it can never change.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD;

    /**
     * Parses a field name ignoring case.
     *
     * @return the matching field, or {@code null} if the name is not modifiable
     */
    public static UserField fromString(String name) {
        if (name == null) {
            return null;
        }
        String normalized = name.trim();
        for (UserField field : values()) {
            if (field.name().equalsIgnoreCase(normalized)) {
                return field;
            }
        }
        return null;
    }

    /** Comma-separated list of the modifiable field names, for messages. */
    public static String allNames() {
        StringBuilder names = new StringBuilder();
        for (UserField field : values()) {
            if (names.length() > 0) {
                names.append(", ");
            }
            names.append(field.name());
        }
        return names.toString();
    }
}
