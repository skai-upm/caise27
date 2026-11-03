package es.upm.etsisi.park.business;

/**
 * Base class of every abnormal situation raised by park operations. The
 * presentation layer reports these to the user and keeps processing commands.
 */
public abstract class ParkException extends Exception {

    protected ParkException(String message) {
        super(message);
    }
}
