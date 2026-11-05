package es.upm.amusementpark.business;

/**
 * INDIVIDUAL attraction: accepts individual tickets only.
 */
public class IndividualAttraction extends Attraction<IndividualAccess> {

    public IndividualAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.INDIVIDUAL;
    }
}
