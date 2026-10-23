package es.upm.park.business.model.attraction;

import es.upm.park.business.exception.AccessDeniedException;
import es.upm.park.business.model.access.GroupAccess;
import es.upm.park.business.model.pass.Pass;
import es.upm.park.business.model.pass.PassRegistry;

import java.util.List;

/**
 * Accepts group tickets only. With SpeedPasses, every ticket of the group
 * ticket's composition must contribute one.
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
    protected List<Pass> passesToConsume(GroupAccess ticket, boolean speed, PassRegistry passes)
            throws AccessDeniedException {
        return speed ? speedPassesFor(ticket.getComposition(), passes) : List.of();
    }
}
