package es.upm.park.business.model;

/**
 * Capability of tickets that admit one user.
 */
public interface IndividualAccess extends Admission {

    User getUser();
}
