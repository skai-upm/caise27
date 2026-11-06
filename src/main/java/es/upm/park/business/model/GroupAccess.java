package es.upm.park.business.model;

import java.util.List;

/** Access capability of a ticket that admits a whole group together. */
public interface GroupAccess extends AccessTicket {

    /** The individual tickets of the members admitted together. */
    List<? extends IndividualAccess> getComposition();
}
