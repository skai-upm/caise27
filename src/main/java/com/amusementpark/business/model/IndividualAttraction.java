package com.amusementpark.business.model;

/**
 * Admits individual tickets only.
 */
public class IndividualAttraction extends Attraction<IndividualAdmission> {

    public IndividualAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAdmission.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.INDIVIDUAL;
    }
}
