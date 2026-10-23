package es.upm.etsisi.park.business.model.ticket;

/** Access capability of a single person holding one individual ticket. */
public interface IndividualAccess extends Access {

    IndividualTicket getIndividualTicket();
}
