package es.upm.amusementpark.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * General access capability: anything that can be presented at an attraction entrance.
 */
public interface AccessTicket {

    String getId();

    LocalDate getDate();

    boolean isValidOn(LocalDate current);

    /**
     * The users admitted together by this ticket.
     */
    List<User> getHolders();

    /**
     * The individual tickets whose passes take part in the entry (one per admitted user).
     */
    List<IndividualTicket> getAdmittedTickets();
}
