package es.upm.etsisi.park.business.controller;

import java.util.Locale;

import es.upm.etsisi.park.business.exception.ValidationException;

/** Small argument checks shared by the controllers. */
final class Checks {

    private Checks() {
    }

    static void requireText(String value, String label) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(label + " must not be empty");
        }
    }

    /** Case-insensitive lookup key for identifiers. */
    static String key(String id) {
        return id.toUpperCase(Locale.ROOT);
    }
}
