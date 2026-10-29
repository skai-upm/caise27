package es.upm.amusementpark.business.model;

import java.util.ArrayList;
import java.util.List;

import es.upm.amusementpark.business.exception.OperationNotAllowedException;

/**
 * High-demand attraction with its own queue: accepts individual tickets only, requires an
 * unconsumed Special Pass (consumed automatically on entry) and does not accept SpeedPasses.
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
    protected List<Pass> selectPasses(IndividualAccess access, boolean speed) {
        if (speed) {
            throw new OperationNotAllowedException("SPECIAL attraction " + getId() + " does not accept SpeedPasses");
        }
        SpecialPass pass = access.getSpecialPass().orElseThrow(() -> new OperationNotAllowedException(
                "ticket " + access.getId() + " has no Special Pass"));
        if (pass.isConsumed()) {
            throw new OperationNotAllowedException("Special Pass " + pass.getId() + " is already consumed");
        }
        List<Pass> passes = new ArrayList<>();
        passes.add(pass);
        return passes;
    }
}
