package es.upm.etsisi.park.business;

/**
 * Read access to the passes of individual tickets, used by attractions to decide
 * which passes an entry consumes.
 */
public interface PassLedger {

    /** An unconsumed SpeedPass of the ticket, or {@code null} if it has none. */
    SpeedPass unusedSpeedPass(IndividualTicket ticket);

    /** The ticket's Special Pass, consumed or not, or {@code null} if it has none. */
    SpecialPass specialPassOf(IndividualTicket ticket);
}
