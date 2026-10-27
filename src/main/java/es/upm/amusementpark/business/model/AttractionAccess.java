package es.upm.amusementpark.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * General access capability: anything that lets its holders into an attraction on a valid date.
 */
public interface AttractionAccess {

    String getId();

    boolean isValidOn(LocalDate day);

    /** Users admitted with this access. */
    List<User> holders();

    /** Individual tickets behind this access (itself, or the composition of a group ticket). */
    List<IndividualTicket> individualTickets();
}
