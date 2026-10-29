package es.upm.amusementpark.business.model.ticket;

import es.upm.amusementpark.business.model.User;

import java.time.LocalDate;
import java.util.List;

/**
 * General access capability: anything that can be presented at an attraction entrance.
 */
public interface AccessTicket {

    String getId();

    boolean isValidOn(LocalDate day);

    /**
     * Users admitted when this ticket is used.
     */
    List<User> getUsers();

    /**
     * The individual tickets whose personal passes apply when this ticket is used.
     */
    List<? extends IndividualAccess> getAccessTickets();
}
