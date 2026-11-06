package es.upm.park.business.model;

/** Access capability of a ticket that admits exactly one user. */
public interface IndividualAccess extends AccessTicket {

    String getOwnerEmail();
}
