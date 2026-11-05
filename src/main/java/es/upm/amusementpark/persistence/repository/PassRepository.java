package es.upm.amusementpark.persistence.repository;

import es.upm.amusementpark.business.exception.RuleViolationException;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.persistence.storage.PassStorage;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stores every pass in a single pass identifier space (not shared with tickets).
 */
public class PassRepository {

    private final Map<String, Pass> passes = new LinkedHashMap<>();
    private final PassStorage storage;

    public PassRepository() {
        this(PassStorage.none(), new TicketRepository());
    }

    public PassRepository(PassStorage storage, TicketRepository tickets) {
        this.storage = storage;
        storage.load(tickets).forEach(p -> passes.put(p.getIdentifier(), p));
    }

    /** Throws if any identifier is already used by an existing pass. */
    public void ensureAvailable(Collection<String> identifiers) {
        for (String id : identifiers) {
            if (passes.containsKey(id)) {
                throw new RuleViolationException("a pass with identifier '" + id + "' already exists");
            }
        }
    }

    public void addAll(List<? extends Pass> newPasses) {
        ensureAvailable(newPasses.stream().map(Pass::getIdentifier).toList());
        newPasses.forEach(p -> passes.put(p.getIdentifier(), p));
        save();
    }

    public void removeAll(Collection<? extends Pass> old) {
        old.forEach(p -> passes.remove(p.getIdentifier()));
        save();
    }

    public Optional<Pass> findById(String identifier) {
        return Optional.ofNullable(passes.get(identifier));
    }

    public List<Pass> findAll() {
        return List.copyOf(passes.values());
    }

    /** Passes associated with a ticket, found by traversal. */
    public List<Pass> passesOf(IndividualTicket ticket) {
        return passes.values().stream().filter(p -> p.getTicket() == ticket).toList();
    }

    /** Persists changes to stored passes (e.g. consumption). */
    public void update() {
        save();
    }

    private void save() {
        storage.save(passes.values());
    }
}
