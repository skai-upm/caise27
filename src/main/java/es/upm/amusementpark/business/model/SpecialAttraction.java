package es.upm.amusementpark.business.model;

import java.util.List;

/**
 * Attraction admitting individual tickets that hold an unused Special Pass, which is
 * consumed on entry. The SPEED option is not accepted.
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
    protected List<Pass> passesToConsume(IndividualAccess access, boolean speed) throws OperationException {
        if (speed) {
            throw new OperationException("SPEED is not accepted at SPECIAL attraction " + getId());
        }
        SpecialPass pass = access.getSpecialPass();
        if (pass == null || pass.isConsumed()) {
            throw new OperationException("Ticket " + access.getId() + " has no unused Special Pass");
        }
        return List.of(pass);
    }
}
