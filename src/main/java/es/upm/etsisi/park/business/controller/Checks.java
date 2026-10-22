package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.business.exception.ParkException;
import es.upm.etsisi.park.business.exception.ValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.function.Supplier;

/**
 * Precondition helpers shared by the controllers.
 */
final class Checks {

    private Checks() {
    }

    static void require(boolean condition, Supplier<? extends ParkException> error) {
        if (!condition) {
            throw error.get();
        }
    }

    static void requireText(String value, String what) {
        require(value != null && !value.isBlank(), () -> new ValidationException(what + " must not be empty"));
    }

    /** Strict ISO {@code yyyy-MM-dd}. */
    static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date (expected yyyy-MM-dd): " + text);
        }
    }

    /** Fails when a singleton controller is used before {@code configure(...)}. */
    static <T> T configured(T dependency, String controller) {
        if (dependency == null) {
            throw new IllegalStateException(controller + " has not been configured");
        }
        return dependency;
    }
}
