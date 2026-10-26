package es.upm.parque.business.model;

/**
 * Access capability of a single user.
 */
public interface IndividualAccess extends Accessible {

    User getUser();
}
