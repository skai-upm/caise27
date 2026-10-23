package es.upm.etsisi.park.user;

/**
 * User attributes that may be changed with USER UPDATE. The DNI is
 * deliberately absent: it cannot be modified.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD;

    /** Resolves a field name ignoring case and surrounding spaces, or returns null. */
    public static UserField fromName(String name) {
        String normalized = name.trim().toUpperCase();
        for (UserField field : values()) {
            if (field.name().equals(normalized)) {
                return field;
            }
        }
        return null;
    }

    /** Field names separated by commas, for help and error messages. */
    public static String names() {
        StringBuilder sb = new StringBuilder();
        for (UserField field : values()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(field.name());
        }
        return sb.toString();
    }
}
