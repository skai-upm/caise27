package es.upm.parque.business.model;

/**
 * Access capability of a single user, which may carry personal passes.
 */
public interface IndividualAccess extends AccessCapability {

    User getUser();

    /**
     * Returns an unconsumed SpeedPass of this access, or {@code null} if there is none.
     */
    SpeedPass findUnusedSpeedPass();

    /**
     * Returns the Special Pass of this access, or {@code null} if it has none.
     */
    SpecialPass getSpecialPass();
}
