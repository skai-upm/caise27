package es.upm.amusementpark.business.model;

/**
 * Accepts individual tickets only.
 */
public class IndividualAttraction extends Attraction<IndividualAccess> {

    public IndividualAttraction(String identifier, String name, int capacity) {
        super(identifier, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.INDIVIDUAL;
    }
}
