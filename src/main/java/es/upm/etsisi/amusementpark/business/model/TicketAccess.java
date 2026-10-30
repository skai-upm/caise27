package es.upm.etsisi.amusementpark.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * General access capability: anything that can admit users to an attraction.
 */
public interface TicketAccess {

    String getId();

    boolean isValidOn(LocalDate today);

    /**
     * Users admitted together by this access.
     */
    List<User> getHolders();

    /**
     * Individual tickets whose passes may be used for this access.
     */
    List<IndividualTicket> getIndividualTickets();
}
