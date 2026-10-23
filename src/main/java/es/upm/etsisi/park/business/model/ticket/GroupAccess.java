package es.upm.etsisi.park.business.model.ticket;

import java.util.List;

/** Access capability of a group entering together with a group ticket. */
public interface GroupAccess extends Access {

    List<IndividualTicket> getMemberTickets();
}
