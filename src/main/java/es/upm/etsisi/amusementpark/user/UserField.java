package es.upm.etsisi.amusementpark.user;

import java.util.Optional;

/**
 * User fields that may be modified with USER UPDATE. The DNI is deliberately absent.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD;

    public static Optional<UserField> fromName(String name) {
        for (UserField field : values()) {
            if (field.name().equals(name)) {
                return Optional.of(field);
            }
        }
        return Optional.empty();
    }
}
