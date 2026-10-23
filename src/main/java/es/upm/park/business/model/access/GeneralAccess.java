package es.upm.park.business.model.access;

import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.User;

import java.time.LocalDate;
import java.util.List;

/**
 * Capability shared by every ticket that can be used to enter an
 * attraction. GENERAL attractions accept any ticket with this capability.
 */
public interface GeneralAccess {

    String getId();

    boolean isValidOn(LocalDate today);

    /**
     * Users who enter when this ticket is used.
     */
    List<User> getHolders();

    /**
     * Individual tickets entering with this ticket; passes are consumed per
     * individual ticket.
     */
    List<IndividualTicket> getAdmittedTickets();
}
