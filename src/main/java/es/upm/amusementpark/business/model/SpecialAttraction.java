package es.upm.amusementpark.business.model;

/**
 * SPECIAL attraction. Admits individual tickets only, under the Special Pass rules.
 */
public class SpecialAttraction extends TypedAttraction<IndividualAccess> {

    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }
}
