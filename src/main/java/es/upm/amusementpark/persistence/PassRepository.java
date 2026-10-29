package es.upm.amusementpark.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;

/**
 * Stores and retrieves SpeedPasses and Special Passes with their price and consumed state.
 */
public interface PassRepository {

    /** Loads passes, resolving each one's individual ticket by identifier. */
    List<Pass> load(Map<String, IndividualTicket> ticketsById);

    void save(Collection<Pass> passes);
}
