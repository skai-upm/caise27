package es.upm.etsisi.amusementpark.business.attraction;

import es.upm.etsisi.amusementpark.business.ticket.IndividualAccess;

/**
 * Accepts individual tickets only, with or without a SpeedPass.
 */
public class IndividualAttraction extends AbstractAttraction<IndividualAccess> {

    public IndividualAttraction(String identifier, String name, int capacity) {
        super(identifier, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.INDIVIDUAL;
    }
}
