package es.upm.parque.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * General access capability: what any attraction needs from a ticket to admit its holders.
 */
public interface GeneralAccess {

    String getId();

    LocalDate getDate();

    /** Users who enter an attraction with this ticket. */
    List<User> getHolders();

    /** The individual tickets whose passes are relevant on entry: the ticket itself or a group's composition. */
    List<IndividualTicket> individualTickets();
}
