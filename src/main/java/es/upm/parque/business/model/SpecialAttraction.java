package es.upm.parque.business.model;

import java.util.List;

/**
 * SPECIAL attraction: a restricted-access activity in high demand. It accepts individual tickets only, like an
 * individual attraction, and additionally requires an unconsumed Special Pass for the ticket, which is consumed
 * when the entry succeeds; the Special Pass condition is this attraction's own behaviour, not a different ticket
 * type. It does not accept SpeedPasses.
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
    public boolean acceptsSpeedPasses() {
        return false;
    }

    /** Selects the ticket's unconsumed Special Pass, used automatically; the SPEED option is not permitted. */
    @Override
    public PassSelection passesToConsume(IndividualAccess ticket, boolean speed, List<IndividualTicket> entering,
            PassLookup passes) {
        if (speed) {
            return PassSelection.refused("special attraction " + getId() + " does not accept SpeedPasses");
        }
        SpecialPass pass = passes.specialPassOf(ticket.individualTicket());
        if (pass == null) {
            return PassSelection.refused("ticket " + ticket.getId() + " has no Special Pass, required by special"
                    + " attraction " + getId());
        }
        if (pass.isConsumed()) {
            return PassSelection.refused("the Special Pass " + pass.getId() + " of ticket " + ticket.getId()
                    + " has already been consumed");
        }
        return PassSelection.ok(List.of(pass));
    }
}
