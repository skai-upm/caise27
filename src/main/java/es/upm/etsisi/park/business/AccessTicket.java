package es.upm.etsisi.park.business;

import java.time.LocalDate;
import java.util.List;

/**
 * General access capability: anything that can be presented at an attraction entrance.
 */
public interface AccessTicket {

    String getId();

    /** Whether the ticket can be used on the given day. */
    boolean isValidOn(LocalDate day);

    /** Individual tickets whose holders enter together when this ticket is used. */
    List<IndividualTicket> getEntryTickets();
}
