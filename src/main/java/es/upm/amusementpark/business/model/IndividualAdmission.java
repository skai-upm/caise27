package es.upm.amusementpark.business.model;

/**
 * Access capability of a single user.
 */
public interface IndividualAdmission extends Admission {

    User getUser();

    /** An unused SpeedPass of this admission, or {@code null} if there is none. */
    SpeedPass findUnusedSpeedPass();

    /** The unused Special Pass of this admission, or {@code null} if there is none. */
    SpecialPass findUnusedSpecialPass();
}
