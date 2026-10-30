package es.upm.amusementpark.business.model;

/**
 * Individual access capability: admits exactly one user through one individual ticket.
 */
public interface IndividualAccess extends AccessTicket {

    IndividualTicket getIndividualTicket();
}
