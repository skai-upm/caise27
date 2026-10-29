package es.upm.etsisi.amusementpark.business.model.attraction;

import es.upm.etsisi.amusementpark.business.model.GeneralAccess;

/**
 * Attraction accepting individual and group tickets, with optional SpeedPass use.
 */
public class GeneralAttraction extends Attraction<GeneralAccess> {

    public GeneralAttraction(String id, String name, int capacity) {
        super(id, name, capacity);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GENERAL;
    }

    @Override
    protected Class<GeneralAccess> accessType() {
        return GeneralAccess.class;
    }
}
