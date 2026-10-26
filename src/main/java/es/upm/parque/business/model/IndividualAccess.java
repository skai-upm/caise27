package es.upm.parque.business.model;

/**
 * Access capability of a single user.
 */
public interface IndividualAccess extends AttractionAccess {

    User getUser();
}
