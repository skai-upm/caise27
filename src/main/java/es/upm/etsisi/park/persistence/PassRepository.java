package es.upm.etsisi.park.persistence;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.business.model.ticket.IndividualTicket;

/** Storage of SpeedPasses and Special Passes, resolved against individual tickets by identifier. */
public interface PassRepository {

    List<Pass> load(Map<String, IndividualTicket> individualTicketsById) throws IOException;

    void save(List<Pass> passes) throws IOException;
}
