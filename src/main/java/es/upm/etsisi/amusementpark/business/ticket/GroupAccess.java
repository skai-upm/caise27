package es.upm.etsisi.amusementpark.business.ticket;

import java.util.List;

/**
 * Joint access of every member included in a group ticket.
 */
public interface GroupAccess extends AccessTicket {
    List<IndividualTicket> getComposition();
}
