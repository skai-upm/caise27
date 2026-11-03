package es.upm.etsisi.amusementpark.business.model;

/** SPECIAL attraction: admits individual tickets only; its Special Pass rules are added on top of individual access. */
public class SpecialAttraction extends Attraction<IndividualAccess> {

    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }
}
