package es.upm.amusementpark.business.rules;

/** Rejection helper shared by the park managers. */
public final class Checks {

    private Checks() {
    }

    /** Rejects the operation with the given reason when the condition does not hold. */
    public static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
