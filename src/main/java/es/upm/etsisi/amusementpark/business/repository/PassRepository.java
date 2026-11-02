package es.upm.etsisi.amusementpark.business.repository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import es.upm.etsisi.amusementpark.business.pass.Pass;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;

/**
 * Stores and retrieves SpeedPasses and Special Passes with their ticket, price and
 * consumed state. Implementations are chosen and injected at start-up.
 */
public interface PassRepository {
    List<Pass> load(Map<String, IndividualTicket> ticketsById) throws IOException;

    void save(List<Pass> passes) throws IOException;

    /**
     * Problems found by the last load (skipped records).
     */
    List<String> getWarnings();
}
