package es.upm.park.business.model;

import java.time.LocalDate;
import java.util.List;

/** Capability of a ticket to give access to attractions (general access). */
public interface AccessTicket {

    String getId();

    /** Whether the ticket can be used on the given day. */
    boolean isValidOn(LocalDate today);

    /** Emails of the users the ticket admits. */
    List<String> admittedUsers();

    /** The individual tickets involved in an entry: the ticket itself or a group's composition. */
    List<? extends IndividualAccess> individualTickets();
}
