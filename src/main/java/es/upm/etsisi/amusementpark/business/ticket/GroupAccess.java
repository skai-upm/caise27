package es.upm.etsisi.amusementpark.business.ticket;

import java.util.List;

/**
 * Group access capability: admits together all users of a group ticket's composition.
 */
public interface GroupAccess extends AccessTicket {

    List<IndividualTicket> composition();
}
