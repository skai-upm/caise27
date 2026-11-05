package es.upm.park.business.model;

/** SPECIAL attraction: admits individual tickets, the basis for the Special Pass rules. */
public class SpecialAttraction extends Attraction<IndividualTicket> {

    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualTicket.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }
}
