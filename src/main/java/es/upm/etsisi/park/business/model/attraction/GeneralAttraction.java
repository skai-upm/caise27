package es.upm.etsisi.park.business.model.attraction;

import java.util.List;

import es.upm.etsisi.park.business.exception.OperationNotAllowedException;
import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.business.model.ticket.Access;

/** Accepts individual and group tickets, optionally with SpeedPasses. */
public class GeneralAttraction extends Attraction<Access> {

    public GeneralAttraction(String id, String name, int capacity) {
        super(id, name, capacity, Access.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GENERAL;
    }

    @Override
    protected List<Pass> passesToConsume(Access access, boolean speed) throws OperationNotAllowedException {
        return speedPasses(access, speed);
    }
}
