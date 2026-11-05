package es.upm.amusementpark.business.model;

import java.util.Optional;

/**
 * Finds the passes of an individual ticket. Supplied to attractions so the model does not depend on storage.
 */
public interface PassLookup {

    /** An unconsumed SpeedPass of the ticket, if any. */
    Optional<Pass> availableSpeedPass(IndividualTicket ticket);

    /** The Special Pass of the ticket (consumed or not), if any. */
    Optional<Pass> specialPass(IndividualTicket ticket);
}
