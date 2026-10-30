package es.upm.etsisi.amusementpark.business.model;

import es.upm.etsisi.amusementpark.business.exception.OperationNotAllowedException;

import java.util.List;

/**
 * Restricted-access attraction for individual tickets holding an unconsumed Special Pass, which is
 * consumed automatically on entry. It runs its own queue, so SpeedPasses are not accepted.
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
    protected List<Pass> passesToConsume(IndividualAccess ticket, boolean speed, PassLookup passes) {
        if (speed) {
            throw new OperationNotAllowedException("Special attraction " + getId() + " does not accept SpeedPasses");
        }
        IndividualTicket individual = ticket.getIndividualTickets().get(0);
        SpecialPass pass = passes.findSpecialPass(individual).orElseThrow(() ->
                new OperationNotAllowedException("Ticket " + individual.getId() + " has no Special Pass"));
        if (pass.isConsumed()) {
            throw new OperationNotAllowedException("Special Pass " + pass.getId() + " is already consumed");
        }
        return List.of(pass);
    }
}
