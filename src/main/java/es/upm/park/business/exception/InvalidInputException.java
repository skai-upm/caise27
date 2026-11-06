package es.upm.park.business.exception;

/** A value supplied to an operation is missing or malformed (dates, prices, capacities, identifiers). */
public class InvalidInputException extends ParkException {

    public InvalidInputException(String message) {
        super(message);
    }
}
