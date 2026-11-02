package com.amusementpark.business.model;

import java.util.List;

/**
 * Admits individual tickets, which must also satisfy the Special Pass rules.
 */
public class SpecialAttraction extends Attraction<IndividualAdmission> {

    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAdmission.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }

    /** Requires the ticket's unused Special Pass; SPEED is not accepted here. */
    @Override
    protected PassSelection selectPasses(IndividualAdmission ticket, boolean speed) {
        if (speed) {
            return PassSelection.failed("SPEED is not accepted at SPECIAL attraction " + getId() + ".");
        }
        SpecialPass pass = ticket.getSpecialPass();
        if (pass == null || pass.isConsumed()) {
            return PassSelection.failed("Ticket " + ticket.getId() + " has no unused Special Pass.");
        }
        return PassSelection.of(List.of(pass));
    }
}
