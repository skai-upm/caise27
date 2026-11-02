package es.upm.etsisi.amusementpark.business.ticket;

/**
 * Individual access capability: admits exactly the user of one individual ticket.
 */
public interface IndividualAccess extends AccessTicket {

    IndividualTicket individualTicket();
}
