package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stores all passes. Pass identifiers are unique among passes.
 */
public class PassRepository {

    private final Map<String, Pass> passes = new LinkedHashMap<>();

    public boolean exists(String identifier) {
        return passes.containsKey(identifier);
    }

    public void save(Pass pass) {
        passes.put(pass.getIdentifier(), pass);
    }

    public Optional<Pass> findById(String identifier) {
        return Optional.ofNullable(passes.get(identifier));
    }

    public boolean delete(String identifier) {
        return passes.remove(identifier) != null;
    }

    public List<Pass> findAll() {
        return new ArrayList<>(passes.values());
    }

    /** Passes associated with the ticket, found by traversing all passes. */
    public List<Pass> findByTicket(IndividualTicket ticket) {
        List<Pass> result = new ArrayList<>();
        for (Pass pass : passes.values()) {
            if (pass.getTicket() == ticket) {
                result.add(pass);
            }
        }
        return result;
    }
}
