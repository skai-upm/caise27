package es.upm.etsisi.amusementpark.business.model;

import java.util.Optional;

/**
 * Finds the passes an attraction may consume on entry.
 */
public interface PassLookup {

    Optional<SpeedPass> findAvailableSpeedPass(IndividualTicket ticket);

    Optional<SpecialPass> findSpecialPass(IndividualTicket ticket);
}
