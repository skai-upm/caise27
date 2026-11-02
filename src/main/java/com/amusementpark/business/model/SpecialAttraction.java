package com.amusementpark.business.model;

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
}
