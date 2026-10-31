package es.upm.amusementpark;

/** Rejection helper shared by the park managers. */
final class Checks {

    private Checks() {
    }

    /** Rejects the operation with the given reason when the condition does not hold. */
    static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
