package es.upm.park.persistence;

import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Pass;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/** Storage of passes, with historical records whose ticket can no longer be found. */
public interface PassRepository {

    PassFileStore.Contents load(Function<String, Optional<IndividualTicket>> ticketById) throws IOException;

    void save(List<Pass> passes, List<String> historicalLines) throws IOException;
}
