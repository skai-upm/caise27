package es.upm.amusementpark.persistence;

import es.upm.amusementpark.model.Ticket;
import es.upm.amusementpark.repository.GroupRepository;
import es.upm.amusementpark.repository.UserRepository;
import java.util.Collection;
import java.util.List;

/**
 * Loads and saves every ticket (individual tickets, group tickets and their compositions).
 */
public interface TicketStorage {

    List<Ticket> load(UserRepository users, GroupRepository groups);

    void save(Collection<Ticket> tickets);

    /** Storage that keeps nothing (in-memory use). */
    static TicketStorage none() {
        return new TicketStorage() {
            @Override
            public List<Ticket> load(UserRepository users, GroupRepository groups) {
                return List.of();
            }

            @Override
            public void save(Collection<Ticket> tickets) {
                // nothing to persist
            }
        };
    }
}
