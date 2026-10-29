package es.upm.amusementpark.business.model;

import java.util.Optional;

/** Queries about the passes of a ticket, provided by central management (tickets do not know their passes). */
public interface PassLookup {

    /** An unconsumed SpeedPass of the ticket, if any (the oldest purchase first). */
    Optional<Pass> unconsumedSpeedPass(IndividualTicket ticket);

    /** The ticket's Special Pass if it exists and has not been consumed. */
    Optional<Pass> unconsumedSpecialPass(IndividualTicket ticket);
}
