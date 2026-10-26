package es.upm.parque.business.model;

/**
 * Admits only individual tickets.
 */
public class IndividualAttraction extends Attraction<IndividualAdmission> {

    public IndividualAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAdmission.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.INDIVIDUAL;
    }
}
