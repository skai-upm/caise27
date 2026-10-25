package es.upm.parque.business.model;

import java.util.List;

/**
 * Admits individual tickets that hold an unconsumed Special Pass, which is consumed on entry. SpeedPasses are
 * not accepted.
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
    protected List<Pass> passesToConsume(IndividualAccess access, boolean speed) {
        if (speed) {
            throw new IllegalStateException("SPEED is not accepted at SPECIAL attraction " + getId());
        }
        SpecialPass pass = access.getSpecialPass();
        if (pass == null) {
            throw new IllegalStateException("Ticket " + access.getId() + " has no Special Pass");
        }
        if (pass.isConsumed()) {
            throw new IllegalStateException("The Special Pass of ticket " + access.getId() + " is already consumed");
        }
        return List.of(pass);
    }
}
