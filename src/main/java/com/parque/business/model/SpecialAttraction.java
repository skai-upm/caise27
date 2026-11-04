package com.parque.business.model;

/**
 * SPECIAL attraction: based on individual tickets, to which it applies its additional Special Pass conditions.
 */
public class SpecialAttraction extends Attraction<IndividualAccess> {
    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }
}
