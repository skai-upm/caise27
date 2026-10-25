package es.upm.parque.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Access capability shared by every kind of ticket: what an attraction needs to let people in.
 */
public interface Admission {

    String getId();

    /** Users admitted together with this admission. */
    List<User> getUsers();

    /** The admission can be used to enter attractions on this day. */
    boolean isValidOn(LocalDate today);

    /** Individual tickets whose personal passes apply to this admission. */
    List<IndividualTicket> getAccessTickets();
}
