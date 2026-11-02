package com.amusementpark.business.model;

/**
 * Access capability of a ticket for exactly one user, which may carry personal passes.
 */
public interface IndividualAdmission extends Admission {

    User getUser();

    /** An unused SpeedPass of this ticket, or {@code null}. */
    SpeedPass nextUnusedSpeedPass();

    /** The ticket's Special Pass, or {@code null}. */
    SpecialPass getSpecialPass();
}
