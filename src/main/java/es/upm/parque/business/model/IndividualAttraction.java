package es.upm.parque.business.model;

/**
 * Admits only individual tickets.
 */
public class IndividualAttraction extends Attraction<IndividualTicket> {

    public IndividualAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualTicket.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.INDIVIDUAL;
    }
}
