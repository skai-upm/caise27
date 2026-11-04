package es.upm.park;

/**
 * Outcome of an operation: success flag plus a human-readable message.
 */
public record Result(boolean success, String message) {

    public static Result ok(String message) {
        return new Result(true, message);
    }

    public static Result error(String message) {
        return new Result(false, message);
    }

    @Override
    public String toString() {
        return (success ? "OK: " : "ERROR: ") + message;
    }
}
