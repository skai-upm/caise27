package es.upm.etsisi.park.business.model.access;

import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.User;

import java.time.LocalDate;
import java.util.List;

/**
 * Capability of being used to enter an attraction.
 */
public interface Access {

    String getId();

    LocalDate getDate();

    boolean isValidOn(LocalDate today);

    /** Users admitted together by this access. */
    List<User> getUsers();

    /** The individual tickets of the admitted users (each one may carry passes). */
    List<IndividualTicket> getAdmittedTickets();
}
