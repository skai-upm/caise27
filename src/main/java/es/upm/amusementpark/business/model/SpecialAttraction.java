package es.upm.amusementpark.business.model;

import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import java.util.List;

/**
 * High-demand attraction for individual tickets. Entry consumes the ticket's Special Pass automatically;
 * SpeedPasses are not accepted because these attractions manage their own queue.
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
    protected List<Pass> passesToConsume(IndividualAccess ticket, boolean speed, PassWallet wallet) {
        if (speed) {
            throw new OperationNotAllowedException("special attraction " + getId() + " does not accept SpeedPasses");
        }
        SpecialPass pass = wallet.specialPassOf(ticket.getIndividualTicket());
        if (pass == null) {
            throw new OperationNotAllowedException("ticket " + ticket.getId() + " has no Special Pass");
        }
        if (pass.isConsumed()) {
            throw new OperationNotAllowedException("the Special Pass " + pass.getId() + " of ticket " + ticket.getId()
                    + " has already been consumed");
        }
        return List.of(pass);
    }
}
