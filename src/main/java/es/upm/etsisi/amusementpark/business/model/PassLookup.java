package es.upm.etsisi.amusementpark.business.model;

/**
 * Read access to the passes of an individual ticket, used by attractions when admitting users.
 */
public interface PassLookup {

    /**
     * Earliest purchased unconsumed SpeedPass of the ticket, or null.
     */
    SpeedPass firstUnconsumedSpeedPass(IndividualTicket ticket);

    /**
     * The Special Pass of the ticket (consumed or not), or null.
     */
    SpecialPass specialPassOf(IndividualTicket ticket);
}
