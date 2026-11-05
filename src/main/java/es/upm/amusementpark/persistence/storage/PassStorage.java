package es.upm.amusementpark.persistence.storage;

import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.persistence.repository.TicketRepository;
import java.util.Collection;
import java.util.List;

/**
 * Loads and saves every pass. Tickets are resolved against the loaded tickets.
 */
public interface PassStorage {

    List<Pass> load(TicketRepository tickets);

    void save(Collection<Pass> passes);

    /** Storage that keeps nothing (in-memory use). */
    static PassStorage none() {
        return new PassStorage() {
            @Override
            public List<Pass> load(TicketRepository tickets) {
                return List.of();
            }

            @Override
            public void save(Collection<Pass> passes) {
                // nothing to persist
            }
        };
    }
}
