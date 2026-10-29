package es.upm.amusementpark.business.model;

import java.util.List;

/** Access capability of a ticket that admits a whole group through its individual tickets. */
public interface GroupAccess extends AccessTicket {

    List<IndividualTicket> getComposition();
}
