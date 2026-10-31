package es.upm.amusementpark.business.model;

import java.time.LocalDate;
import java.util.List;

/** Capability of a ticket to give access to attractions. */
public interface AccessTicket {

    String getId();

    /** Whether the ticket can be used to enter on the given date. */
    boolean isValidOn(LocalDate today);

    /** The individual tickets whose users enter when this ticket is used. */
    List<IndividualTicket> getAccessTickets();
}
