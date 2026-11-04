package es.upm.etsisi.amusementpark.business.model;

/** GENERAL attraction: admits individual and group tickets. */
public class GeneralAttraction extends Attraction<AttractionAccess> {

    public GeneralAttraction(String id, String name, int capacity) {
        super(id, name, capacity);
    }

    @Override
    protected Class<AttractionAccess> accessType() {
        return AttractionAccess.class;
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GENERAL;
    }
}
