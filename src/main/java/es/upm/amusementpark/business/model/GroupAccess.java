package es.upm.amusementpark.business.model;

import java.util.List;

/**
 * Group access capability: admits every member of a group ticket's composition at once.
 */
public interface GroupAccess extends AccessTicket {

    List<IndividualTicket> getComposition();
}
