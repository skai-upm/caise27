package es.upm.etsisi.park.business;

import java.util.List;

/**
 * Access capability of a ticket that admits a whole group at once.
 */
public interface GroupAccess extends AccessTicket {

    List<IndividualTicket> getComposition();
}
