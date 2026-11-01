package es.upm.amusementpark.business.model;

/**
 * Admits individual tickets, which must also hold an unused Special Pass.
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
