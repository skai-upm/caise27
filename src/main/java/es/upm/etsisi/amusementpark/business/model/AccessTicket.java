package es.upm.etsisi.amusementpark.business.model;

import java.time.LocalDate;
import java.util.List;

/** Capability of a ticket to give access to attractions: identity, date of validity and the users it admits. */
public interface AccessTicket {

    String getId();

    LocalDate getDate();

    /** The registered users admitted with this ticket. */
    List<User> getVisitors();
}
