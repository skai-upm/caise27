package es.upm.parque.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Something that grants access to an attraction on a date.
 */
public interface AccessCredential {

    String getId();

    LocalDate getDate();

    /**
     * Users admitted with this credential.
     */
    List<User> getUsers();

    /**
     * Individual tickets this credential is made of (itself for an individual ticket).
     */
    List<IndividualTicket> getIndividualTickets();
}
