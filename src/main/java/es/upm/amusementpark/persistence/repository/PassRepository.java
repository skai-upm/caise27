package es.upm.amusementpark.persistence.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.persistence.PassFileStore;

/**
 * Stores SpeedPasses and Special Passes in one identifier space.
 */
public class PassRepository {

    private final Map<String, Pass> passesById = new LinkedHashMap<>();
    private final PassFileStore store;

    /** In-memory repository without persistence. */
    public PassRepository() {
        this.store = null;
    }

    public PassRepository(PassFileStore store, TicketRepository tickets) {
        this.store = store;
        for (Pass pass : store.load(tickets)) {
            passesById.put(pass.getId(), pass);
        }
    }

    /** Persists the current passes, including state changes made on the pass objects. */
    public void save() {
        if (store != null) {
            store.save(passesById.values());
        }
    }

    public boolean exists(String id) {
        return passesById.containsKey(id);
    }

    public Pass findById(String id) {
        return passesById.get(id);
    }

    public void addAll(Collection<? extends Pass> passes) {
        for (Pass pass : passes) {
            passesById.put(pass.getId(), pass);
        }
        save();
    }

    public void remove(Pass pass) {
        passesById.remove(pass.getId());
        save();
    }

    public List<Pass> findByTicket(IndividualTicket ticket) {
        List<Pass> result = new ArrayList<>();
        for (Pass pass : passesById.values()) {
            if (pass.getTicket() == ticket) {
                result.add(pass);
            }
        }
        return result;
    }

    public List<Pass> findAll() {
        return new ArrayList<>(passesById.values());
    }
}
