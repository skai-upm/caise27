package es.upm.etsisi.amusementpark.business.repository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.ticket.Ticket;
import es.upm.etsisi.amusementpark.business.user.User;

/**
 * Stores and retrieves top-level tickets (group tickets carry their composition). Implementations are chosen and injected at start-up.
 */
public interface TicketRepository {
    List<Ticket> load(Map<String, User> usersByDni, Map<String, Group> groupsById) throws IOException;

    void save(List<Ticket> tickets) throws IOException;

    /**
     * Problems found by the last load (skipped records).
     */
    List<String> getWarnings();
}
