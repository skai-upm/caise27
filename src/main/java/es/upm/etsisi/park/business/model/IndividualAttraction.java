package es.upm.etsisi.park.business.model;

/**
 * Accepts individual tickets only, with optional SpeedPass use.
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
