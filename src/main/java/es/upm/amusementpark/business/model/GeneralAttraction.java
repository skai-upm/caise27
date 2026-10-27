package es.upm.amusementpark.business.model;

/**
 * GENERAL attraction. Accepts individual and group tickets.
 */
public class GeneralAttraction extends Attraction<AttractionAccess> {

    public GeneralAttraction(String id, String name, int capacity) {
        super(id, name, capacity, AttractionAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GENERAL;
    }
}
