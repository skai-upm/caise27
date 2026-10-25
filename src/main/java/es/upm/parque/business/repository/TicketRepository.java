package es.upm.parque.business.repository;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;

/**
 * Storage of individual and group tickets. Owners and groups are linked through the given lookups.
 */
public interface TicketRepository {

    List<Ticket> load(Function<String, User> userByDni, Function<String, Group> groupById);

    void save(Collection<Ticket> tickets);
}
