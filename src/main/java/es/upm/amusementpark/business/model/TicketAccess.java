package es.upm.amusementpark.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * General capability of a ticket to give access to attractions.
 */
public interface TicketAccess {

    String getIdentifier();

    LocalDate getDate();

    boolean isValidOn(LocalDate day);

    /** Users admitted when this ticket is used. */
    List<User> admittedUsers();

    /** Individual tickets this access is made of (itself, or the composition of a group ticket). */
    List<IndividualTicket> individualTickets();
}
