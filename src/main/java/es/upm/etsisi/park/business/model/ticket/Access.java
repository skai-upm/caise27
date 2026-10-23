package es.upm.etsisi.park.business.model.ticket;

import java.time.LocalDate;
import java.util.List;

import es.upm.etsisi.park.business.model.user.User;

/**
 * General access capability: something that can be presented at an
 * attraction entrance. Attractions accepting any ticket type are
 * parameterised with this interface.
 */
public interface Access {

    String getId();

    boolean isValidOn(LocalDate day);

    /** Users admitted together with this access. */
    List<User> getHolders();

    /** The individual tickets behind this access, one per admitted user. */
    List<IndividualTicket> getIndividualTickets();
}
