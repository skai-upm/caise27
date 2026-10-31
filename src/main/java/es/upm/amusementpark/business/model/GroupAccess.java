package es.upm.amusementpark.business.model;

import java.util.List;

/** Access for all the members of a group at once, through its constituent tickets. */
public interface GroupAccess extends AccessTicket {

    Group getGroup();

    List<IndividualTicket> getTickets();
}
