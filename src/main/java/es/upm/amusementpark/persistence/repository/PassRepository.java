package es.upm.amusementpark.persistence.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;

/**
 * Stores passes of both types in one identifier space, in purchase order.
 */
public class PassRepository {

    private final Map<String, Pass> passes = new LinkedHashMap<>();

    public void save(Pass pass) {
        passes.put(pass.getId(), pass);
    }

    public Optional<Pass> findById(String id) {
        return Optional.ofNullable(passes.get(id));
    }

    public List<Pass> findByTicket(IndividualTicket ticket) {
        return passes.values().stream().filter(p -> p.getTicket() == ticket).toList();
    }

    public void delete(String id) {
        passes.remove(id);
    }

    public List<Pass> findAll() {
        return new ArrayList<>(passes.values());
    }
}
