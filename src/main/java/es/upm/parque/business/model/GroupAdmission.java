package es.upm.parque.business.model;

import java.util.List;

/**
 * Access capability of a group, exercised through the individual tickets of its members.
 */
public interface GroupAdmission extends Admission {

    List<IndividualTicket> getTickets();
}
