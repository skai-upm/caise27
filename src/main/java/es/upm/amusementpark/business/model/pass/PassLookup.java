package es.upm.amusementpark.business.model.pass;

import es.upm.amusementpark.business.model.ticket.IndividualAccess;

import java.util.Optional;

/**
 * Finds the passes associated with an individual ticket (tickets do not reference their passes).
 */
public interface PassLookup {

    /**
     * The earliest purchased unconsumed SpeedPass of the ticket, if any.
     */
    Optional<SpeedPass> availableSpeedPass(IndividualAccess ticket);

    /**
     * The Special Pass of the ticket (consumed or not), if any.
     */
    Optional<SpecialPass> specialPassOf(IndividualAccess ticket);
}
