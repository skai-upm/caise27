package es.upm.amusementpark.business.model;

/**
 * Read access to the passes of individual tickets, used by attractions to
 * decide which passes an entry needs.
 */
public interface PassWallet {

    /** An unconsumed SpeedPass of {@code ticket}, or {@code null} if there is none. */
    SpeedPass availableSpeedPass(IndividualAccess ticket);

    /** The unconsumed Special Pass of {@code ticket}, or {@code null} if it has none or it was used. */
    SpecialPass unconsumedSpecialPass(IndividualAccess ticket);
}
