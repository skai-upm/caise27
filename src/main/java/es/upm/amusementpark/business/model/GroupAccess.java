package es.upm.amusementpark.business.model;

import java.util.List;

/**
 * Group access capability: a ticket that admits all users of its composition
 * together.
 */
public interface GroupAccess extends AccessTicket {

    List<IndividualTicket> getComposition();
}
