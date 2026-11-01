package es.upm.amusementpark.business.model;

import java.util.List;

/**
 * Access for all members of a group at once.
 */
public interface GroupAccess extends AttractionAccess {

    Group getGroup();

    /**
     * The member tickets composing this access.
     */
    List<IndividualTicket> getTickets();
}
