package es.upm.etsisi.park.business;

/**
 * Capability of a ticket admitting exactly one user, accepted by individual and
 * special attractions.
 */
public interface IndividualAccess extends GeneralAccess {

    IndividualTicket individualTicket();
}
