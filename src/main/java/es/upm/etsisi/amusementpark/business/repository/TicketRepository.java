package es.upm.etsisi.amusementpark.business.repository;

import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;
import java.util.List;
import java.util.function.Function;

/**
 * Persistent store of individual and group tickets, including historical ones.
 */
public interface TicketRepository {

    /**
     * Loads tickets, resolving active users by DNI and existing groups by identifier. Users that
     * are no longer active are rebuilt from the stored ticket data.
     */
    List<Ticket> load(Function<String, User> userByDni, Function<String, Group> groupById);

    void save(List<Ticket> tickets);
}
