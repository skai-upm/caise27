package es.upm.amusementpark.business.model;

import java.util.Optional;

/**
 * User attributes that may be modified with USER UPDATE. The DNI is intentionally absent.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD;

    /** Finds a field by its exact name, or empty when it is not modifiable. */
    public static Optional<UserField> fromName(String name) {
        for (UserField f : values()) {
            if (f.name().equals(name)) {
                return Optional.of(f);
            }
        }
        return Optional.empty();
    }
}
