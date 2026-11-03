package es.upm.etsisi.amusementpark.business.ticket;

/**
 * Access capability of a ticket that admits exactly one user.
 */
public interface IndividualAccess extends AccessTicket {

    IndividualTicket getIndividualTicket();
}
