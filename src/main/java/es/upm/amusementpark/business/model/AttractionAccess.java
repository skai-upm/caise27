package es.upm.amusementpark.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Capability of a ticket to admit people into an attraction.
 */
public interface AttractionAccess {

    String getId();

    /**
     * True if the ticket gives access on the given date.
     */
    boolean isValidOn(LocalDate date);

    /**
     * Users admitted by this access.
     */
    List<User> getUsers();

    /**
     * Individual tickets that admit those users, one per user; personal passes hang from them.
     */
    List<IndividualTicket> getAccessTickets();
}
