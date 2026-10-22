package es.upm.etsisi.park.business.model.attraction;

import es.upm.etsisi.park.business.model.access.IndividualAccess;

/**
 * Accepts individual tickets only, optionally with a SpeedPass.
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
