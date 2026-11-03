package es.upm.etsisi.park.business;

import java.util.List;

/**
 * Accepts individual tickets that have an unconsumed Special Pass, which is used
 * automatically. SpeedPasses are not accepted: special attractions have their own queue.
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
    protected List<Pass> passesToConsume(IndividualAccess access, boolean speed, PassLedger passes)
            throws ParkException {
        if (speed) {
            throw new OperationNotAllowedException("SPECIAL attraction " + getId() + " does not accept SpeedPasses");
        }
        IndividualTicket ticket = access.individualTicket();
        SpecialPass pass = passes.specialPassOf(ticket);
        if (pass == null) {
            throw new OperationNotAllowedException("ticket " + ticket.getId() + " has no Special Pass");
        }
        if (pass.isConsumed()) {
            throw new OperationNotAllowedException("Special Pass " + pass.getId() + " has already been consumed");
        }
        return List.of(pass);
    }
}
