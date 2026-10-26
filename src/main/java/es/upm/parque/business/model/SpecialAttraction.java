package es.upm.parque.business.model;

/**
 * Admits individual tickets, on which the Special Pass rules apply.
 */
public class SpecialAttraction extends Attraction<IndividualAdmission> {

    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAdmission.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }
}
