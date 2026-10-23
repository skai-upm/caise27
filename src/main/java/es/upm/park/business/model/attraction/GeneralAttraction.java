package es.upm.park.business.model.attraction;

import es.upm.park.business.model.access.GeneralAccess;

/**
 * Accepts individual and group tickets, with or without SpeedPasses.
 */
public class GeneralAttraction extends Attraction<GeneralAccess> {

    public GeneralAttraction(String id, String name, int capacity) {
        super(id, name, capacity, GeneralAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GENERAL;
    }
}
