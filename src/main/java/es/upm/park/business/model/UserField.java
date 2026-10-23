package es.upm.park.business.model;

/**
 * User attributes that may be changed with USER UPDATE. The DNI is
 * deliberately absent because it is immutable.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD;

    /**
     * Parses a field name ignoring case and surrounding whitespace.
     *
     * @return the matching field, or {@code null} if the name is not modifiable
     */
    public static UserField parse(String name) {
        if (name == null) {
            return null;
        }
        String normalized = name.trim().toUpperCase();
        for (UserField field : values()) {
            if (field.name().equals(normalized)) {
                return field;
            }
        }
        return null;
    }

    public static String listNames() {
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
