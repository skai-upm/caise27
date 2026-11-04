package es.upm.park;

/**
 * Outcome of a command: success flag, a human-readable message and whether command
 * processing must stop after it.
 */
public record Result(boolean success, String message, boolean terminate) {

    public Result(boolean success, String message) {
        this(success, message, false);
    }

    public static Result ok(String message) {
        return new Result(true, message);
    }

    public static Result error(String message) {
        return new Result(false, message);
    }

    /** Successful result that ends command processing. */
    public static Result exit(String message) {
        return new Result(true, message, true);
    }

    @Override
    public String toString() {
        return (success ? "OK: " : "ERROR: ") + message;
    }
}
