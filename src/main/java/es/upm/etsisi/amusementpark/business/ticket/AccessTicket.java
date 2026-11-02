package es.upm.etsisi.amusementpark.business.ticket;

import java.time.LocalDate;
import java.util.List;

import es.upm.etsisi.amusementpark.business.user.User;

/**
 * Capability of a ticket to give access to attractions. Attractions are generic in
 * a subtype of this interface, which fixes the tickets they can handle.
 */
public interface AccessTicket {
    String getIdentifier();

    boolean isValidOn(LocalDate day);

    /**
     * The users who enter with this ticket.
     */
    List<User> getUsers();

    /**
     * The individual tickets that enter: the ticket itself or a group's composition.
     */
    List<IndividualTicket> getEntryTickets();
}
