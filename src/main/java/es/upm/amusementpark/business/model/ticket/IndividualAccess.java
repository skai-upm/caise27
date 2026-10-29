package es.upm.amusementpark.business.model.ticket;

import es.upm.amusementpark.business.model.User;

/**
 * Individual access capability: a ticket that admits exactly one user.
 */
public interface IndividualAccess extends AccessTicket {

    User getUser();
}
