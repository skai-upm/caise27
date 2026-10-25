package es.upm.parque.business.model;

/**
 * Admits individual and group tickets.
 */
public class GeneralAttraction extends Attraction<AccessCapability> {

    public GeneralAttraction(String id, String name, int capacity) {
        super(id, name, capacity, AccessCapability.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GENERAL;
    }
}
