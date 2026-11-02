package es.upm.etsisi.park.business;

/**
 * Restricted-access attraction: individual tickets with an unconsumed Special Pass, never SpeedPasses.
 */
public class SpecialAttraction extends Attraction<IndividualAccess> {

    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }

    @Override
    public boolean acceptsSpeedPass() {
        return false;
    }

    @Override
    public boolean requiresSpecialPass() {
        return true;
    }
}
