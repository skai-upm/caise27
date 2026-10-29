package es.upm.amusementpark.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Capability shared by every ticket that can be used to enter an attraction.
 */
public interface GeneralAccess {

    String getId();

    boolean isValidOn(LocalDate today);

    /** Users admitted by this ticket. */
    List<User> getHolders();

    /** Individual tickets that enter (and may consume passes) when this ticket is used. */
    List<IndividualTicket> getEntryTickets();
}
