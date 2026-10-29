package es.upm.etsisi.amusementpark.business.repository;

import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Pass;
import java.util.List;
import java.util.function.Function;

/**
 * Persistent store of SpeedPasses and Special Passes.
 */
public interface PassRepository {

    /**
     * Loads passes, resolving their individual tickets (including composition tickets) by identifier.
     */
    List<Pass> load(Function<String, IndividualTicket> ticketById);

    void save(List<Pass> passes);
}
