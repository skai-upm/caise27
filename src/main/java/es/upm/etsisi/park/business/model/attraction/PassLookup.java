package es.upm.etsisi.park.business.model.attraction;

import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.pass.SpecialPass;
import es.upm.etsisi.park.business.model.pass.SpeedPass;

import java.util.Optional;

/**
 * Finds the passes an attraction needs to check or consume on entry.
 */
public interface PassLookup {

    /** Any unconsumed SpeedPass of the ticket. */
    Optional<SpeedPass> availableSpeedPass(IndividualTicket ticket);

    /** The ticket's Special Pass, consumed or not. */
    Optional<SpecialPass> specialPass(IndividualTicket ticket);
}
