package com.amusementpark.business.model;

/**
 * Admits individual tickets that present their unused Special Pass; SpeedPasses are not accepted.
 */
public class SpecialAttraction extends Attraction<IndividualTicket> {

    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualTicket.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }

    @Override
    public boolean allowsSpeed() {
        return false;
    }

    @Override
    public boolean requiresSpecialPass() {
        return true;
    }
}
