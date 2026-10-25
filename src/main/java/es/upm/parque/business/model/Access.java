package es.upm.parque.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Capability of granting access to an attraction on a date.
 */
public interface Access {

    String getId();

    LocalDate getDate();

    /**
     * Users admitted by this access.
     */
    List<User> getUsers();

    /**
     * Individual tickets involved in this access (used to locate their personal passes).
     */
    List<IndividualTicket> getIndividualTickets();
}
