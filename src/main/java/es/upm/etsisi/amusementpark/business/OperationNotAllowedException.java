package es.upm.etsisi.amusementpark.business;

/**
 * The operation is valid in form but forbidden by the current park state (locks, dates, capacity, passes...).
 */
public class OperationNotAllowedException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public OperationNotAllowedException(String message) {
        super(message);
    }
}
