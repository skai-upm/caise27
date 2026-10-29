package es.upm.amusementpark.business.repository;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.model.Ticket;

import java.util.Collection;
import java.util.List;

/** Replaceable storage of the park's tickets. */
public interface TicketRepository {

    void saveAll(Collection<Ticket> tickets);

    /**
     * Rebuilds the stored tickets into the park through its reconstruction methods.
     *
     * @param skipped receives stored entries that could not be read
     */
    void loadInto(ParkController park, List<String> skipped);
}
