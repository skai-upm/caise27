package es.upm.amusementpark.business.repository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;

/**
 * Storage of individual and group tickets; holders and groups are resolved
 * against the already loaded users (by DNI) and groups (by identifier).
 */
public interface TicketRepository {

    List<Ticket> load(Map<String, User> usersByDni, Map<String, Group> groupsById) throws IOException;

    void save(List<Ticket> tickets) throws IOException;
}
