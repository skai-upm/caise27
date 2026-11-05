package es.upm.amusementpark.business.model;

import java.util.List;

/**
 * Access capability of a ticket that admits a group through its composition of individual accesses.
 */
public interface GroupAccess extends TicketAccess {

    List<IndividualTicket> getComposition();
}
