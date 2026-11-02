package es.upm.etsisi.amusementpark.business;

/**
 * Root of the exceptions that signal a park operation could not be completed.
 * Every subclass guarantees that the rejected operation left the state unchanged.
 */
public abstract class ParkException extends Exception {
    protected ParkException(String message) {
        super(message);
    }
}
