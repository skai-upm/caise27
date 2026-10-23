package es.upm.amusementpark.business.model;

import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import java.util.List;

/**
 * Accepts group tickets only. With SPEED, every individual ticket of the
 * group ticket's composition uses one SpeedPass.
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
    public List<Pass> requiredPasses(GroupAccess ticket, boolean speed, PassWallet wallet)
            throws OperationNotAllowedException {
        return speed ? oneSpeedPassEach(ticket.getComposition(), wallet) : List.of();
    }
}
