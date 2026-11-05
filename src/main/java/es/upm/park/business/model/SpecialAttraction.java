package es.upm.park.business.model;

/**
 * SPECIAL attraction: admits individual tickets only; every entry needs the ticket's unused
 * Special Pass and the SPEED option is not accepted.
 */
public class SpecialAttraction extends Attraction<IndividualTicket> {

    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualTicket.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }

    @Override
    public boolean allowsSpeed() {
        return false;
    }

    @Override
    public boolean requiresSpecialPass() {
        return true;
    }
}
