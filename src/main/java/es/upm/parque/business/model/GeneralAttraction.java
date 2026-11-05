package es.upm.parque.business.model;

/**
 * GENERAL attraction. Accepts individual and group tickets.
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
