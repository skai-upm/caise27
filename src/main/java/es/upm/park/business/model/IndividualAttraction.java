package es.upm.park.business.model;

/** INDIVIDUAL attraction: admits individual tickets only. */
public class IndividualAttraction extends Attraction<IndividualTicket> {

    public IndividualAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualTicket.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.INDIVIDUAL;
    }
}
