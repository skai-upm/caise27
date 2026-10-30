package es.upm.amusementpark.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Capability of a ticket to give access to attractions.
 */
public interface AccessTicket {

    String getId();

    LocalDate getDate();

    /** True if the ticket can be used to enter attractions on the given date. */
    boolean isValidOn(LocalDate today);

    /** Users admitted by the ticket. */
    List<User> getUsers();

    /** Individual tickets admitted, one per user: the ticket itself or the tickets composing it. */
    List<IndividualAccess> getAccessUnits();
}
