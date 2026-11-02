package es.upm.etsisi.amusementpark.business.ticket;

import es.upm.etsisi.amusementpark.business.user.User;

/**
 * Access for exactly one person through one individual ticket.
 */
public interface IndividualAccess extends AccessTicket {
    User getUser();
}
