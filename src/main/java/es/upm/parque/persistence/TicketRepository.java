package es.upm.parque.persistence;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.User;

/**
 * Storage of individual tickets, group tickets and the personal passes attached to individual tickets.
 */
public interface TicketRepository {

    /**
     * Tickets restored from storage: individually bought tickets and group tickets (with their internal tickets).
     */
    record Loaded(List<IndividualTicket> individualTickets, List<GroupTicket> groupTickets) {
    }

    Loaded load(Function<String, User> userByDni, Function<String, Group> groupById) throws IOException;

    void saveAll(List<IndividualTicket> individualTickets, List<GroupTicket> groupTickets) throws IOException;
}
