package es.upm.etsisi.park.business.model.attraction;

import java.util.List;

import es.upm.etsisi.park.business.exception.OperationNotAllowedException;
import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.business.model.ticket.IndividualAccess;

/** Accepts individual tickets only, optionally with a SpeedPass. */
public class IndividualAttraction extends Attraction<IndividualAccess> {

    public IndividualAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.INDIVIDUAL;
    }

    @Override
    protected List<Pass> passesToConsume(IndividualAccess access, boolean speed) throws OperationNotAllowedException {
        return speedPasses(access, speed);
    }
}
