package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Stores and retrieves every SpeedPass and Special Pass, consumed or not.
 */
public interface PassRepository {

    /** Loads all passes, resolving their individual tickets by identifier. */
    List<Pass> loadAll(Function<String, IndividualTicket> ticketById) throws IOException;

    void saveAll(Collection<Pass> passes) throws IOException;
}
