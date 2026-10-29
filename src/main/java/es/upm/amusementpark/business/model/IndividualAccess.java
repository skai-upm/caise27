package es.upm.amusementpark.business.model;

/** Access capability of a ticket that admits a single user. */
public interface IndividualAccess extends AccessTicket {

    User getUser();
}
