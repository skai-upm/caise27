package es.upm.etsisi.park.business.model;

/**
 * User attributes that may be changed with USER UPDATE. The DNI is deliberately absent.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD;

    /**
     * Case-insensitive lookup; returns null when the text names no updatable field.
     */
    public static UserField fromText(String text) {
        if (text == null) {
            return null;
        }
        String normalized = text.trim();
        for (UserField field : values()) {
            if (field.name().equalsIgnoreCase(normalized)) {
                return field;
            }
        }
        return null;
    }
}
