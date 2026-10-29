package es.upm.amusementpark.business.model.ticket;

import java.util.List;

/**
 * Group access capability: a ticket that admits all the members of its composition together.
 */
public interface GroupAccess extends AccessTicket {

    List<? extends IndividualAccess> getComposition();
}
