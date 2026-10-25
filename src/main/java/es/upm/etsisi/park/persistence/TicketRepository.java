package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.business.model.Group;
import es.upm.etsisi.park.business.model.Ticket;
import es.upm.etsisi.park.business.model.User;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Storage for individual and group tickets, including historical ones.
 */
public interface TicketRepository {

    List<Ticket> loadAll(Function<String, Optional<User>> userByDni, Function<String, Optional<Group>> groupById);

    void saveAll(Collection<Ticket> tickets);
}
