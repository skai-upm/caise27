package es.upm.amusementpark.business;

/**
 * Access capability of individual tickets (one user).
 */
public interface IndividualAccess extends GeneralAccess {

    String getUserEmail();
}
