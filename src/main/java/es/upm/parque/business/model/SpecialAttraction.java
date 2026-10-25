package es.upm.parque.business.model;

import java.util.List;

/**
 * Admits individual tickets that carry an unused Special Pass, which the entry consumes.
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
    protected List<Pass> passesToConsume(IndividualTicket ticket, boolean speed) {
        if (speed) {
            throw new IllegalArgumentException("SPEED is not accepted at SPECIAL attraction " + getId());
        }
        SpecialPass pass = ticket.getSpecialPass();
        if (pass == null) {
            throw new IllegalArgumentException("Ticket " + ticket.getId() + " has no Special Pass");
        }
        if (pass.isConsumed()) {
            throw new IllegalArgumentException("Special Pass of ticket " + ticket.getId() + " was already used");
        }
        return List.of(pass);
    }
}
