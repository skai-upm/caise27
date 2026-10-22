package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.pass.Pass;

import java.util.Collection;
import java.util.List;

/**
 * SpeedPasses and Special Passes, sharing one identifier space.
 */
public interface PassRepository extends Repository<Pass> {

    List<Pass> findByTicket(IndividualTicket ticket);

    /** Adds all passes and persists once (all or nothing in memory). */
    void addAll(Collection<? extends Pass> passes);

    /** Removes all passes and persists once. */
    void removeAll(Collection<? extends Pass> passes);
}
