package es.upm.etsisi.amusementpark.business.model;

/** SPECIAL attraction: admits individual tickets, the basis for its Special Pass rules. */
public class SpecialAttraction extends Attraction<IndividualAccess> {

    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity);
    }

    @Override
    protected Class<IndividualAccess> accessType() {
        return IndividualAccess.class;
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }
}
