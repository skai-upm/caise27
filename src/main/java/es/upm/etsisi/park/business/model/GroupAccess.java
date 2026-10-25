package es.upm.etsisi.park.business.model;

import java.util.List;

/**
 * Access capability of a ticket that lets a whole group in at once.
 */
public interface GroupAccess extends AccessTicket {

    List<IndividualTicket> getTickets();
}
