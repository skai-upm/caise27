package es.upm.amusementpark.business.model;

/**
 * Individual access capability: a ticket that admits one user.
 */
public interface IndividualAccess extends AccessTicket {

    User getUser();
}
