package es.upm.parque.persistence;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.User;

/**
 * Storage abstraction for group tickets. Implementations decide where and how data is kept.
 */
public interface GroupTicketRepository {

    /**
     * Loads the stored group tickets. Unknown groups and users become detached historical records.
     */
    List<GroupTicket> load(Function<String, Group> groupById, Function<String, User> userByDni) throws IOException;

    void save(Collection<GroupTicket> tickets) throws IOException;
}
