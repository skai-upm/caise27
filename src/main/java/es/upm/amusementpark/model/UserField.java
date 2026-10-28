package es.upm.amusementpark.model;

import java.util.Optional;

/**
 * User attributes that may be modified. The DNI is deliberately absent.
 */
public enum UserField {
    NAME,
    SURNAME,
    EMAIL,
    PASSWORD,
    PHONE,
    CARD;

    /**
     * Returns the field with exactly the given name, if it is modifiable.
     */
    public static Optional<UserField> fromName(String name) {
        for (UserField field : values()) {
            if (field.name().equals(name)) {
                return Optional.of(field);
            }
        }
        return Optional.empty();
    }
}
