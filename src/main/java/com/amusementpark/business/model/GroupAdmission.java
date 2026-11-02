package com.amusementpark.business.model;

import java.util.List;

/**
 * Access capability of a ticket that admits a whole group through its constituent tickets.
 */
public interface GroupAdmission extends Admission {

    List<IndividualTicket> getTickets();
}
