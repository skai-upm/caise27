package es.upm.parque.business.repository;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;

import java.util.List;
import java.util.function.Function;

/**
 * Storage of individual and group tickets; holders and groups are relinked through the given lookups.
 */
public interface TicketRepository {

    List<Ticket> load(Function<String, User> userByDni, Function<String, Group> groupById);

    void save(List<Ticket> tickets);
}
