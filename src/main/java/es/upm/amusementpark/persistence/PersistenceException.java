package es.upm.amusementpark.persistence;

/** Abnormal situation while reading or writing persistent park data. */
public class PersistenceException extends RuntimeException {

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
