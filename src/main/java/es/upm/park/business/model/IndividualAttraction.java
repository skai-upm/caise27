package es.upm.park.business.model;

/** Admits individual tickets only. */
public class IndividualAttraction extends Attraction<IndividualAccess> {

    public IndividualAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType type() {
        return AttractionType.INDIVIDUAL;
    }
}
