package es.upm.amusementpark.business.model.attraction;

import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.model.pass.Pass;
import es.upm.amusementpark.business.model.pass.PassLookup;
import es.upm.amusementpark.business.model.pass.SpecialPass;
import es.upm.amusementpark.business.model.ticket.IndividualAccess;

import java.util.List;

/**
 * High-demand attraction for individual tickets only. Entry automatically consumes the ticket's
 * unconsumed Special Pass; SpeedPasses are not accepted because it manages its own queue.
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
    protected List<Pass> selectPasses(IndividualAccess ticket, boolean speed, PassLookup passes)
            throws OperationNotAllowedException {
        if (speed) {
            throw new OperationNotAllowedException("SpeedPasses are not accepted at special attraction " + getId());
        }
        SpecialPass pass = passes.specialPassOf(ticket).orElseThrow(() ->
                new OperationNotAllowedException("ticket has no Special Pass: " + ticket.getId()));
        if (pass.isConsumed()) {
            throw new OperationNotAllowedException("Special Pass already consumed: " + pass.getId());
        }
        return List.of(pass);
    }
}
