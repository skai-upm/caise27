package es.upm.parque.business.model;

import java.util.List;

/**
 * Access capability of a group, made of the individual tickets of its members.
 */
public interface GroupAccess extends Accessible {

    List<IndividualTicket> getTickets();
}
