package es.upm.etsisi.amusementpark.business.model;

/**
 * Individual access capability: a ticket that admits a single user.
 */
public interface IndividualAdmission extends Admission {

    User getUser();
}
