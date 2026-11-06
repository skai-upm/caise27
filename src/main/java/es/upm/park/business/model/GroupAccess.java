package es.upm.park.business.model;

import java.util.List;

/**
 * Capability of tickets that admit a group through its constituent
 * individual tickets.
 */
public interface GroupAccess extends Admission {

    List<IndividualTicket> getComposition();
}
