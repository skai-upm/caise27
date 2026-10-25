package es.upm.etsisi.park.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * General attraction access capability shared by every ticket kind.
 */
public interface AccessTicket {

    String getId();

    /**
     * Users that enter an attraction with this ticket.
     */
    List<User> getHolders();

    boolean isValidOn(LocalDate day);

    /**
     * Individual tickets that enter with this ticket; their passes are the ones used at entry.
     */
    List<IndividualTicket> getAccessTickets();
}
