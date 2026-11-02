package es.upm.etsisi.park.business;

/**
 * Access capability of a ticket that admits exactly one person.
 */
public interface IndividualAccess extends AccessTicket {

    User getUser();
}
