package es.upm.parque.business.model;

/**
 * Access capability of a single user's ticket.
 */
public interface IndividualAdmission extends Admission {

    User getUser();
}
