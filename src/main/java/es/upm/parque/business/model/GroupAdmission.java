package es.upm.parque.business.model;

import java.util.List;

/**
 * Access capability of a ticket that admits a group through its individual tickets.
 */
public interface GroupAdmission extends Admission {

    List<IndividualTicket> getTickets();
}
