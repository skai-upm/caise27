package es.upm.etsisi.park.business.model;

/**
 * Access capability of a ticket that lets exactly one user in.
 */
public interface IndividualAccess extends AccessTicket {

    IndividualTicket getIndividualTicket();
}
