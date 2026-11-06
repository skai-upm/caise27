package es.upm.park.business.model;

import java.util.Optional;

/** Pass information and actions that attractions need while admitting tickets. */
public interface EntryRules {

    /** Identifier of an unconsumed SpeedPass of the ticket, if any. */
    Optional<String> availableSpeedPass(IndividualAccess ticket);

    /** Identifier of the unconsumed Special Pass of the ticket, if any. */
    Optional<String> availableSpecialPass(IndividualAccess ticket);

    /** Marks a pass as consumed. */
    void consume(String passId);
}
