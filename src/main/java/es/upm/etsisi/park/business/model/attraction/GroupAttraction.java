package es.upm.etsisi.park.business.model.attraction;

import java.util.List;

import es.upm.etsisi.park.business.exception.OperationNotAllowedException;
import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.business.model.ticket.GroupAccess;

/**
 * Accepts group tickets only. With SPEED, every member ticket of the
 * composition needs its own unconsumed SpeedPass.
 */
public class GroupAttraction extends Attraction<GroupAccess> {

    public GroupAttraction(String id, String name, int capacity) {
        super(id, name, capacity, GroupAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GROUP;
    }

    @Override
    protected List<Pass> passesToConsume(GroupAccess access, boolean speed) throws OperationNotAllowedException {
        return speedPasses(access, speed);
    }
}
