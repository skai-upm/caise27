package es.upm.amusementpark.business.model;

/**
 * Access capability of a single user's ticket.
 */
public interface IndividualAccess extends AttractionAccess {

    User getUser();
}
