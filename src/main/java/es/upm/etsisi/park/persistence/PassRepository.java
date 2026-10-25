package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.Pass;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Storage for SpeedPasses and Special Passes.
 */
public interface PassRepository {

    List<Pass> loadAll(Function<String, Optional<IndividualTicket>> ticketById);

    void saveAll(Collection<Pass> passes);
}
