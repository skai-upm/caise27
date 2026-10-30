package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.IndividualTicket;

import java.io.IOException;
import java.util.List;

/**
 * Storage of the personal passes attached to individual tickets.
 */
public interface PassRepository {

    /** Restores the stored passes, attaching each one to its ticket among {@code tickets}. */
    void load(List<IndividualTicket> tickets) throws IOException;

    /** Stores every pass of the given tickets. */
    void save(List<IndividualTicket> tickets) throws IOException;
}
