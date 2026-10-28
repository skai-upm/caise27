package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.exception.ParkException;

/**
 * A command line is malformed: wrong number of arguments or values that cannot be interpreted.
 */
public class CommandSyntaxException extends ParkException {

    private static final long serialVersionUID = 1L;

    public CommandSyntaxException(String message) {
        super(message);
    }
}
