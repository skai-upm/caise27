package es.upm.amusementpark.business.model;

/**
 * Accepts individual and group tickets; both may use SpeedPasses.
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
