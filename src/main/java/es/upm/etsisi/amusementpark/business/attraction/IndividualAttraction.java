package es.upm.etsisi.amusementpark.business.attraction;

import es.upm.etsisi.amusementpark.business.ticket.IndividualAccess;

/**
 * Accepts individual tickets only, with an optional SpeedPass.
 */
public class IndividualAttraction extends Attraction<IndividualAccess> {

    public IndividualAttraction(String identifier, String name, int capacity) {
        super(identifier, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.INDIVIDUAL;
    }
}
