package es.upm.parque.business.model;

import java.util.List;

/**
 * Individual access capability: a ticket for a single user, whose own passes can be checked on entry.
 */
public interface IndividualAccess extends GeneralAccess {

    IndividualTicket individualTicket();

    @Override
    default List<IndividualTicket> individualTickets() {
        return List.of(individualTicket());
    }
}
