package es.upm.etsisi.amusementpark.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Capability of a ticket to give access to attractions that accept any ticket type.
 */
public interface GeneralAccess {

    String getId();

    boolean isValidOn(LocalDate day);

    /**
     * Individual tickets whose users enter when this access is used.
     */
    List<IndividualTicket> entryTickets();
}
