package es.upm.etsisi.amusementpark.business.model.attraction;

import es.upm.etsisi.amusementpark.business.model.IndividualAccess;

/**
 * Attraction accepting individual tickets only, with optional SpeedPass use.
 */
public class IndividualAttraction extends Attraction<IndividualAccess> {

    public IndividualAttraction(String id, String name, int capacity) {
        super(id, name, capacity);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.INDIVIDUAL;
    }

    @Override
    protected Class<IndividualAccess> accessType() {
        return IndividualAccess.class;
    }
}
