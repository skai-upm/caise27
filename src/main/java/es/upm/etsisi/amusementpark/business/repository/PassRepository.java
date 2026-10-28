package es.upm.etsisi.amusementpark.business.repository;

import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Pass;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Persistent store of SpeedPasses and Special Passes.
 */
public interface PassRepository {

    /**
     * @param tickets resolves an individual ticket identifier (standalone or composed)
     */
    List<Pass> load(Function<String, Optional<IndividualTicket>> tickets);

    void save(Collection<Pass> passes);

    List<String> getProblems();
}
