package es.upm.amusementpark.business.model;

/**
 * Access capability of a single user.
 */
public interface IndividualAdmission extends Admission {

    User getUser();
}
