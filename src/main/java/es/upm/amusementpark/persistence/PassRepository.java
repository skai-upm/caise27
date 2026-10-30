package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import java.util.Collection;
import java.util.List;

/**
 * SpeedPasses and Special Passes, in one identifier space.
 */
public interface PassRepository {

    List<Pass> findAll();

    Pass findById(String id);

    boolean exists(String id);

    List<Pass> passesOf(IndividualTicket ticket);

    void addAll(Collection<? extends Pass> passes);

    void removeAll(Collection<? extends Pass> passes);

    /**
     * Persists changes made to passes already held by the repository (e.g. consumption).
     */
    void save();
}
