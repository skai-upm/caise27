package es.upm.amusementpark.business.repository;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.ticket.Ticket;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Stores and retrieves all tickets; each loaded group ticket is followed by its composition tickets.
 */
public interface TicketRepository {

    List<Ticket> load(Map<String, User> activeUsersByDni, Map<String, Group> groupsById) throws IOException;

    void saveAll(Collection<Ticket> tickets, Predicate<User> isActiveUser) throws IOException;
}
