package es.upm.etsisi.amusementpark.business.model;

/** SPECIAL attraction: admits individual tickets only; its Special Pass rules are added on top of individual access. */
public class SpecialAttraction extends Attraction<IndividualAccess> {

    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAccess.class);
    }

    /** SpeedPasses cannot be used at SPECIAL attractions. */
    @Override
    public boolean acceptsSpeed() {
        return false;
    }

    /** Every entry needs the ticket's unused Special Pass, consumed on success. */
    @Override
    public boolean requiresSpecialPass() {
        return true;
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }
}
