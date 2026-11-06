package es.upm.park.persistence.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Pass;

/**
 * Keeps the passes of the park in their own identifier space.
 */
public class PassRepository {

    private final List<Pass> passes = new ArrayList<>();

    public boolean existsId(String id) {
        return findById(id).isPresent();
    }

    public void addAll(List<? extends Pass> newPasses) {
        passes.addAll(newPasses);
    }

    public void removeAll(List<? extends Pass> removed) {
        passes.removeAll(removed);
    }

    public Optional<Pass> findById(String id) {
        return passes.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public List<Pass> findByTicket(IndividualTicket ticket) {
        return passes.stream().filter(p -> p.getTicket() == ticket).toList();
    }

    public List<Pass> findAll() {
        return Collections.unmodifiableList(passes);
    }
}
