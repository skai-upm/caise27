package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Pass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Stores SpeedPasses and Special Passes, which share one identifier space.
 */
public class PassRepository {

    private final PassFileStore store;
    private final List<Pass> passes;

    public PassRepository(PassFileStore store) {
        this.store = store;
        this.passes = new ArrayList<>(store.load());
    }

    public Optional<Pass> findById(String id) {
        return passes.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public boolean existsId(String id) {
        return findById(id).isPresent();
    }

    public List<Pass> findByTicket(IndividualTicket ticket) {
        return passes.stream().filter(p -> p.getTicket() == ticket).toList();
    }

    public List<Pass> findAll() {
        return List.copyOf(passes);
    }

    public void addAll(Collection<? extends Pass> newPasses) {
        passes.addAll(newPasses);
        save();
    }

    public void removeAll(Collection<? extends Pass> removed) {
        passes.removeAll(removed);
        save();
    }

    public void save() {
        store.save(passes);
    }
}
