package es.upm.amusementpark.business;

/**
 * GENERAL attraction: accepts individual and group tickets.
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
