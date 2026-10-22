package es.upm.etsisi.park.business.model.access;

import es.upm.etsisi.park.business.model.IndividualTicket;

import java.util.List;

/**
 * Joint access for all members of a group purchase, accepted by group attractions.
 */
public interface GroupAccess extends GeneralAccess {

    List<IndividualTicket> getTickets();
}
