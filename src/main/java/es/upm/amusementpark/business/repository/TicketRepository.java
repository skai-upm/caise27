package es.upm.amusementpark.business.repository;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;

/**
 * Storage of top-level tickets (individual and group tickets with their constituents).
 */
public interface TicketRepository {

    List<Ticket> load(Function<String, User> userByDni, Function<String, Group> groupById) throws IOException;

    void save(List<Ticket> tickets) throws IOException;
}
