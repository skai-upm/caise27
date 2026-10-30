package es.upm.etsisi.amusementpark.business.model;

/**
 * Access capability of a ticket that admits exactly one user.
 */
public interface IndividualAccess extends TicketAccess {

    User getUser();
}
