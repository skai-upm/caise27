package com.amusementpark.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Access capability of a ticket: what an attraction needs to admit its holders.
 */
public interface Admission {

    String getId();

    LocalDate getDate();

    boolean isValidOn(LocalDate today);

    /** The users admitted by this ticket. */
    List<User> getHolders();

    /** The individual admissions this ticket consists of (itself, or each constituent). */
    List<IndividualAdmission> getAdmissions();
}
