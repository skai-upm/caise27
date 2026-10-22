package es.upm.etsisi.park.service;

/**
 * Precondition helper for services.
 */
final class Checks {

    private Checks() {
    }

    static void require(boolean condition, String message) {
        if (!condition) {
            throw new UserOperationException(message);
        }
    }
}
