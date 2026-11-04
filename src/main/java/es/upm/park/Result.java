package es.upm.park;

/**
 * Outcome of a command: success flag, a human-readable message, whether command processing
 * must stop after it, and whether the message is printed verbatim (without the OK/ERROR prefix).
 */
public record Result(boolean success, String message, boolean terminate, boolean verbatim) {

    public Result(boolean success, String message) {
        this(success, message, false, false);
    }

    public static Result ok(String message) {
        return new Result(true, message);
    }

    public static Result error(String message) {
        return new Result(false, message);
    }

    /** Successful result that ends command processing. */
    public static Result exit(String message) {
        return new Result(true, message, true, false);
    }

    /** Result whose text is printed exactly as given, for outputs with a prescribed format. */
    public static Result verbatim(boolean success, String text) {
        return new Result(success, text, false, true);
    }

    @Override
    public String toString() {
        if (verbatim) {
            return message;
        }
        return (success ? "OK: " : "ERROR: ") + message;
    }
}
