package es.upm.etsisi.amusementpark.business.ticket;

import java.time.LocalDate;
import java.util.List;

/**
 * General access capability: a ticket that can be presented at an attraction entrance.
 */
public interface AccessTicket {

    String getId();

    boolean isValidOn(LocalDate current);

    /** The individual tickets whose users enter when this ticket is used. */
    List<IndividualTicket> getIndividualTickets();
}
