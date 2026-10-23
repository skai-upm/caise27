package es.upm.amusementpark.business.model;

import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import java.util.List;

/**
 * Restricted-access attraction for individual tickets. Entry automatically
 * uses the ticket's unconsumed Special Pass; SpeedPasses are not accepted
 * because the attraction manages its own queue.
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
    public List<Pass> requiredPasses(IndividualAccess ticket, boolean speed, PassWallet wallet)
            throws OperationNotAllowedException {
        if (speed) {
            throw new OperationNotAllowedException("Special attraction " + getId() + " does not accept SpeedPasses");
        }
        SpecialPass pass = wallet.unconsumedSpecialPass(ticket);
        if (pass == null) {
            throw new OperationNotAllowedException("Ticket " + ticket.getId()
                    + " has no unconsumed Special Pass; nobody entered");
        }
        return List.of(pass);
    }
}
