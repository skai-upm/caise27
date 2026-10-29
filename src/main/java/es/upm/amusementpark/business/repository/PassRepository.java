package es.upm.amusementpark.business.repository;

import es.upm.amusementpark.business.model.pass.Pass;
import es.upm.amusementpark.business.model.ticket.Ticket;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Stores and retrieves SpeedPasses and Special Passes; tickets are resolved by identifier.
 */
public interface PassRepository {

    List<Pass> load(Map<String, Ticket> ticketsById) throws IOException;

    void saveAll(Collection<Pass> passes) throws IOException;
}
