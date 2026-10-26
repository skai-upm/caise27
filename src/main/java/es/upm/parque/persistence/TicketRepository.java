package es.upm.parque.persistence;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Stores and restores individual and group tickets, linking them to the registered users and groups.
 */
public interface TicketRepository {

    /**
     * @param userByDni resolves a DNI to the registered user, or null if it no longer exists
     * @param groupById resolves a group identifier to the group, or null if it no longer exists
     */
    List<Ticket> load(Function<String, User> userByDni, Function<String, Group> groupById);

    void save(Collection<Ticket> tickets);
}
