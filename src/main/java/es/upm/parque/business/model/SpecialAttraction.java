package es.upm.parque.business.model;

import java.util.List;

/**
 * Special attraction: admits individual tickets that carry an unused Special Pass, which the entry consumes.
 * SpeedPasses are not accepted.
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
            throw new IllegalArgumentException("SPEED is not accepted at SPECIAL attraction " + getId());
        }
        SpecialPass special = access.getIndividualTickets().get(0).getSpecialPass();
        if (special == null) {
            throw new IllegalArgumentException("ticket " + access.getId() + " has no Special Pass");
        }
        if (special.isConsumed()) {
            throw new IllegalArgumentException("the Special Pass of ticket " + access.getId() + " is already used");
        }
        return List.of(special);
    }
}
