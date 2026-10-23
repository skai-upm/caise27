package es.upm.park.business.model.access;

import es.upm.park.business.model.IndividualTicket;

import java.util.List;

/**
 * Access capability of a ticket that admits a whole group at once. GROUP
 * attractions only handle tickets with this capability.
 */
public interface GroupAccess extends GeneralAccess {

    List<IndividualTicket> getComposition();
}
