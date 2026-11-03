package es.upm.etsisi.park.business;

import java.util.List;

/**
 * Capability of a ticket to be used at attractions that accept any ticket kind.
 */
public interface GeneralAccess {

    /** The individual tickets whose users enter when this ticket is used. */
    List<IndividualTicket> entryTickets();
}
