package es.upm.park.business.model.attraction;

import es.upm.park.business.model.access.IndividualAccess;

/**
 * Accepts individual tickets only, with or without a SpeedPass.
 */
public class IndividualAttraction extends Attraction<IndividualAccess> {

    public IndividualAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.INDIVIDUAL;
    }
}
