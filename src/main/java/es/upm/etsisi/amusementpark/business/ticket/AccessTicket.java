package es.upm.etsisi.amusementpark.business.ticket;

import es.upm.etsisi.amusementpark.business.User;

import java.util.List;

/**
 * General access capability: anything that lets users into an attraction.
 */
public interface AccessTicket {

    /** Individual tickets that enter, one per admitted user. */
    List<IndividualTicket> getEntryTickets();

    /** Users admitted when this ticket is used. */
    List<User> getHolders();
}
