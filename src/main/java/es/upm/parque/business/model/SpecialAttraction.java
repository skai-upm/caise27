package es.upm.parque.business.model;

import es.upm.parque.business.ParkException;
import java.util.List;

/**
 * Attraction admitting individual tickets only; entering consumes the ticket's Special Pass and
 * SpeedPasses cannot be used.
 */
public class SpecialAttraction extends Attraction<IndividualAdmission> {

    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAdmission.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }

    @Override
    protected boolean allowsSpeed() {
        return false;
    }

    /**
     * The ticket's own Special Pass, which must exist and not be consumed yet.
     */
    @Override
    protected List<Pass> passesFor(IndividualAdmission ticket, boolean speed) {
        SpecialPass pass = ticket.getSpecialPass();
        if (pass == null) {
            throw new ParkException("Ticket " + ticket.getId() + " has no Special Pass");
        }
        if (pass.isConsumed()) {
            throw new ParkException("The Special Pass of ticket " + ticket.getId() + " has already been consumed");
        }
        return List.of(pass);
    }
}
