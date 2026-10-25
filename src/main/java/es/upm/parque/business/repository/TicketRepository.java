package es.upm.parque.business.repository;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.User;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Storage of individual and group tickets.
 */
public interface TicketRepository {

    /**
     * Tickets read from storage.
     */
    record Loaded(List<IndividualTicket> individualTickets, List<GroupTicket> groupTickets) {
    }

    Loaded load(Function<String, User> usersByDni, Function<String, Group> groupsById);

    void saveAll(Collection<IndividualTicket> individualTickets, Collection<GroupTicket> groupTickets);
}
