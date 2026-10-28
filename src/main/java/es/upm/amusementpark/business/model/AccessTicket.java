package es.upm.amusementpark.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Capability of being used to access attractions: a date of validity and the individual tickets
 * whose users enter.
 */
public interface AccessTicket {

    String getId();

    LocalDate getDate();

    List<IndividualTicket> individualTickets();
}
