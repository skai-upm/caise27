package es.upm.amusementpark.business.model;

import java.util.List;

/**
 * Access capability of a ticket that admits all the members of a group purchase together.
 */
public interface GroupAccess extends GeneralAccess {

    List<IndividualTicket> getComposition();
}
