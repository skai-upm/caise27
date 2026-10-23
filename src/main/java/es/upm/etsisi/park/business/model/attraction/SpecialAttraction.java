package es.upm.etsisi.park.business.model.attraction;

import java.util.List;

import es.upm.etsisi.park.business.exception.OperationNotAllowedException;
import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.business.model.pass.SpecialPass;
import es.upm.etsisi.park.business.model.ticket.IndividualAccess;
import es.upm.etsisi.park.business.model.ticket.IndividualTicket;

/**
 * Restricted-access attraction for individual tickets. Entry automatically
 * consumes the ticket's Special Pass; it has its own queue, so SpeedPasses
 * are not accepted.
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
    protected List<Pass> passesToConsume(IndividualAccess access, boolean speed) throws OperationNotAllowedException {
        if (speed) {
            throw new OperationNotAllowedException("Special attraction " + getId() + " does not accept SpeedPasses");
        }
        IndividualTicket ticket = access.getIndividualTicket();
        SpecialPass pass = ticket.getSpecialPass();
        if (pass == null) {
            throw new OperationNotAllowedException("Ticket " + ticket.getId() + " has no Special Pass");
        }
        if (pass.isConsumed()) {
            throw new OperationNotAllowedException("Special Pass " + pass.getId() + " of ticket " + ticket.getId()
                    + " is already consumed");
        }
        return List.of(pass);
    }
}
