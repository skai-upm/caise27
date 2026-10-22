package es.upm.etsisi.park.business.model.attraction;

import es.upm.etsisi.park.business.model.access.GeneralAccess;

/**
 * Accepts individual and group tickets, optionally with SpeedPasses.
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
