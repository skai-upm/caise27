package es.upm.amusementpark.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * General access capability: a ticket that admits users to attractions on its
 * date.
 */
public interface AccessTicket {

    String getIdentifier();

    LocalDate getDate();

    /** The users this ticket admits to an attraction. */
    List<User> getHolders();
}
