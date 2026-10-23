package es.upm.amusementpark.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Capability of every ticket to give access to attractions that accept any
 * kind of ticket.
 */
public interface GeneralAccess {

    String getId();

    LocalDate getDate();

    /** Whether the ticket can be used on {@code today}. */
    boolean isValidOn(LocalDate today);

    /** Users who enter with this ticket. */
    List<User> getHolders();

    /** Individual tickets that enter, whose passes may be used for the entry. */
    List<IndividualAccess> getEntryTickets();
}
