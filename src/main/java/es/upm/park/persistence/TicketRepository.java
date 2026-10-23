package es.upm.park.persistence;

import es.upm.park.business.model.Group;
import es.upm.park.business.model.Ticket;
import es.upm.park.business.model.User;

import java.util.List;
import java.util.function.Function;

/**
 * Storage of individual and group tickets, including historical ones.
 */
public interface TicketRepository {

    /**
     * Loads top-level tickets (standalone individual tickets and group
     * tickets with their composition).
     *
     * @param userByDni resolves active users; unknown DNIs become historical users
     * @param groupById resolves existing groups; unknown ids leave the reference empty
     */
    List<Ticket> loadAll(Function<String, User> userByDni, Function<String, Group> groupById);

    void saveAll(Iterable<Ticket> tickets);
}
