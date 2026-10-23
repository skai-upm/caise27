package es.upm.etsisi.park.persistence;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import es.upm.etsisi.park.business.model.group.Group;
import es.upm.etsisi.park.business.model.ticket.Ticket;
import es.upm.etsisi.park.business.model.user.User;

/**
 * Storage of top-level tickets (individual tickets and group tickets with
 * their composition). Users that are no longer active are stored as
 * snapshots so historical tickets can be rebuilt.
 */
public interface TicketRepository {

    List<Ticket> load(Map<String, User> activeUsersByDni, Map<String, Group> groupsById) throws IOException;

    void save(List<Ticket> tickets, Collection<User> activeUsers) throws IOException;
}
