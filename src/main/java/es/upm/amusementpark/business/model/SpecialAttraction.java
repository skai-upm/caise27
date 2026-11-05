package es.upm.amusementpark.business.model;

/**
 * SPECIAL attraction: accepts individual tickets only; entry also depends on the ticket's Special Pass.
 */
public class SpecialAttraction extends Attraction<IndividualAccess> {

    public SpecialAttraction(String identifier, String name, int capacity) {
        super(identifier, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }
}
