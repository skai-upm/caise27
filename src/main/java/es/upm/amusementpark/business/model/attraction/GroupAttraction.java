package es.upm.amusementpark.business.model.attraction;

import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.model.pass.Pass;
import es.upm.amusementpark.business.model.pass.PassLookup;
import es.upm.amusementpark.business.model.ticket.GroupAccess;

import java.util.List;

/**
 * Accepts group tickets only. With SPEED, every ticket of the composition must provide a SpeedPass.
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
    protected List<Pass> selectPasses(GroupAccess ticket, boolean speed, PassLookup passes)
            throws OperationNotAllowedException {
        return speed ? speedPassesFor(ticket.getComposition(), passes) : List.of();
    }
}
