package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.ParkException;

/**
 * Thrown when a command line is unknown, has the wrong number of elements or a value
 * that cannot be parsed.
 */
public class InvalidCommandException extends ParkException {
    public InvalidCommandException(String message) {
        super(message);
    }
}
