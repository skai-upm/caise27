package es.upm.etsisi.amusementpark.business.model;

/** INDIVIDUAL attraction: admits only individual tickets. */
public class IndividualAttraction extends Attraction<IndividualAccess> {

    public IndividualAttraction(String id, String name, int capacity) {
        super(id, name, capacity);
    }

    @Override
    protected Class<IndividualAccess> accessType() {
        return IndividualAccess.class;
    }

    @Override
    public AttractionType getType() {
        return AttractionType.INDIVIDUAL;
    }
}
