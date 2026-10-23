package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Stores and retrieves the full set of tickets, including historical ones.
 */
public interface TicketRepository {

    /** Loads all tickets, resolving holders by DNI and purchasing groups by identifier. */
    List<Ticket> loadAll(Function<String, User> userByDni, Function<String, Group> groupById) throws IOException;

    void saveAll(Collection<Ticket> tickets) throws IOException;
}
