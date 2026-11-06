package es.upm.park.business.model;

import java.util.List;

/**
 * Accepts individual tickets only; the ticket's user enters.
 */
public class IndividualAttraction extends Attraction<IndividualAccess> {

    public IndividualAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.INDIVIDUAL;
    }

    @Override
    public List<User> holdersOf(IndividualAccess ticket) {
        return List.of(ticket.getUser());
    }

    @Override
    public List<IndividualAccess> speedPassTickets(IndividualAccess ticket) {
        return List.of(ticket);
    }
}
