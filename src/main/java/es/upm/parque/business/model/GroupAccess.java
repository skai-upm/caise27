package es.upm.parque.business.model;

import java.util.List;

/**
 * Group access capability: a ticket for a whole group, made of the individual tickets of its members.
 */
public interface GroupAccess extends GeneralAccess {

    List<IndividualTicket> getComposition();

    @Override
    default List<IndividualTicket> individualTickets() {
        return getComposition();
    }
}
