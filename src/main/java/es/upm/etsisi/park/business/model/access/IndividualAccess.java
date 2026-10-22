package es.upm.etsisi.park.business.model.access;

import es.upm.etsisi.park.business.model.IndividualTicket;

/**
 * Access for a single person, accepted by individual and special attractions.
 */
public interface IndividualAccess extends GeneralAccess {

    IndividualTicket getTicket();
}
