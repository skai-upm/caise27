package es.upm.amusementpark.business.model;

/**
 * Access capability of a ticket for a single user, which may carry personal passes.
 */
public interface IndividualAccess extends AccessTicket {

    User getUser();

    /** First SpeedPass not yet consumed, or {@code null}. */
    SpeedPass findUnusedSpeedPass();

    /** The Special Pass of the ticket, or {@code null}. */
    SpecialPass getSpecialPass();
}
