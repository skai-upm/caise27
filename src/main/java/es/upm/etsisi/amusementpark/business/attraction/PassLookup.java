package es.upm.etsisi.amusementpark.business.attraction;

import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.ticket.SpecialPass;
import es.upm.etsisi.amusementpark.business.ticket.SpeedPass;

import java.util.Optional;

/**
 * Gives attractions access to the passes managed by the park.
 */
public interface PassLookup {

    Optional<SpeedPass> findUnusedSpeedPass(IndividualTicket ticket);

    Optional<SpecialPass> findSpecialPass(IndividualTicket ticket);
}
