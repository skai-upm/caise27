package es.upm.park.persistence.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import es.upm.park.business.model.IndividualAccess;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Pass;
import es.upm.park.persistence.PassFileStore;

/**
 * Keeps the passes of the park in their own identifier space, optionally
 * backed by a file store loaded on creation.
 */
public class PassRepository {

    private final List<Pass> passes = new ArrayList<>();
    private final PassFileStore store;

    public PassRepository() {
        this.store = null;
    }

    /** Loads stored passes, linking them to their individual tickets. */
    public PassRepository(PassFileStore store, Function<String, Optional<IndividualTicket>> ticketById) {
        this.store = store;
        passes.addAll(store.load(ticketById));
    }

    public boolean existsId(String id) {
        return findById(id).isPresent();
    }

    /** Adds passes, writing the file first when persistent. */
    public void addAll(List<? extends Pass> newPasses) {
        List<Pass> snapshot = new ArrayList<>(passes);
        snapshot.addAll(newPasses);
        persist(snapshot);
        passes.addAll(newPasses);
    }

    /** Removes passes, writing the file first when persistent. */
    public void removeAll(List<? extends Pass> removed) {
        if (removed.isEmpty()) {
            return;
        }
        List<Pass> snapshot = new ArrayList<>(passes);
        snapshot.removeAll(removed);
        persist(snapshot);
        passes.removeAll(removed);
    }

    /** Writes the current passes after an in-place change (consumption). */
    public void save() {
        persist(passes);
    }

    public Optional<Pass> findById(String id) {
        return passes.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public List<Pass> findByTicket(IndividualAccess ticket) {
        return passes.stream().filter(p -> p.getTicket() == ticket).toList();
    }

    public List<Pass> findAll() {
        return Collections.unmodifiableList(passes);
    }

    private void persist(List<Pass> snapshot) {
        if (store != null) {
            store.save(snapshot);
        }
    }
}
