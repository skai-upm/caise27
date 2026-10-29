package es.upm.amusementpark.presentation;

/** A command line whose syntax (keyword or number of fields) cannot be processed. */
public class InvalidCommandException extends RuntimeException {

    public InvalidCommandException(String message) {
        super(message);
    }
}
