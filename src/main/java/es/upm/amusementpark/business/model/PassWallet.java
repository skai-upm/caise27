package es.upm.amusementpark.business.model;

/**
 * Gives attractions access to the passes bought for an individual ticket.
 */
public interface PassWallet {

    /**
     * An unconsumed SpeedPass of the ticket, or null if there is none.
     */
    SpeedPass availableSpeedPass(IndividualTicket ticket);

    /**
     * The Special Pass of the ticket (consumed or not), or null if it never bought one.
     */
    SpecialPass specialPassOf(IndividualTicket ticket);
}
