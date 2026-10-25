package es.upm.parque.business.model;

import java.util.List;

/**
 * Admits individual tickets that carry an unused Special Pass, which is consumed on entry. SPEED is not allowed.
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
    protected String checkSpeed(IndividualAccess access) {
        return "SPEED is not accepted at SPECIAL attraction " + getId();
    }

    @Override
    protected String checkSpecific(IndividualAccess access) {
        SpecialPass pass = access.getSpecialPass();
        if (pass == null) {
            return "ticket " + access.getId() + " has no Special Pass";
        }
        if (pass.isConsumed()) {
            return "the Special Pass of ticket " + access.getId() + " has already been used";
        }
        return null;
    }

    @Override
    protected List<Pass> consumeSpecific(IndividualAccess access) {
        SpecialPass pass = access.getSpecialPass();
        pass.consume();
        return List.of(pass);
    }
}
