package es.upm.amusementpark.persistence;

import java.util.Collection;
import java.util.Map;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;

/**
 * Stores and retrieves individual and group tickets, including historical ones.
 */
public interface TicketRepository {

    /** Loads top-level tickets by identifier, resolving users and groups where they still exist. */
    Map<String, Ticket> load(Map<String, User> usersByDni, Map<String, Group> groupsById);

    void save(Collection<Ticket> tickets);
}
