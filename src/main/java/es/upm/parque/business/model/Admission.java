package es.upm.parque.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Access capability offered by any ticket: it is valid on a date and admits some users.
 */
public interface Admission {

    String getId();

    LocalDate getDate();

    List<User> getVisitors();

    /**
     * The individual tickets this admission covers: the ticket itself, or every ticket of a group.
     */
    List<IndividualTicket> getIndividualTickets();
}
