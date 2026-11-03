package es.upm.etsisi.park.business;

import java.util.List;

/**
 * Capability of a ticket admitting a whole group at once, accepted by group attractions.
 */
public interface GroupAccess extends GeneralAccess {

    List<IndividualTicket> getComposition();
}
