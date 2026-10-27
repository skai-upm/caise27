package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.IndividualAccess;
import es.upm.amusementpark.business.model.Pass;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stores all passes. Pass identifiers are unique among passes. When a
 * {@link PassFileStorage} is given, passes are loaded from it on creation and
 * saved to it after every change.
 */
public class PassRepository {

    private final Map<String, Pass> passes = new LinkedHashMap<>();
    private final PassFileStorage storage;

    /** Creates a repository that keeps passes in memory only. */
    public PassRepository() {
        this.storage = null;
    }

    public PassRepository(PassFileStorage storage, TicketRepository tickets) {
        this.storage = storage;
        for (Pass pass : storage.load(tickets)) {
            passes.put(pass.getIdentifier(), pass);
        }
    }

    public boolean exists(String identifier) {
        return passes.containsKey(identifier);
    }

    public void save(Pass pass) {
        passes.put(pass.getIdentifier(), pass);
        persist();
    }

    public Optional<Pass> findById(String identifier) {
        return Optional.ofNullable(passes.get(identifier));
    }

    public boolean delete(String identifier) {
        boolean removed = passes.remove(identifier) != null;
        persist();
        return removed;
    }

    /** Writes the current passes to the storage, if any. */
    public void persist() {
        if (storage != null) {
            storage.save(passes.values());
        }
    }

    public List<Pass> findAll() {
        return new ArrayList<>(passes.values());
    }

    /** Passes associated with the ticket, found by traversing all passes. */
    public List<Pass> findByTicket(IndividualAccess ticket) {
        List<Pass> result = new ArrayList<>();
        for (Pass pass : passes.values()) {
            if (pass.getTicket() == ticket) {
                result.add(pass);
            }
        }
        return result;
    }
}
