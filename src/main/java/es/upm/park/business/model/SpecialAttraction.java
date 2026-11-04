package es.upm.park.business.model;

/** Admits individual tickets, the basis for its Special Pass rules. */
public class SpecialAttraction extends Attraction<IndividualAccess> {

    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType type() {
        return AttractionType.SPECIAL;
    }
}
