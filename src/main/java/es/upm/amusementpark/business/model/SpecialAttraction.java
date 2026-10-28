package es.upm.amusementpark.business.model;

import java.util.ArrayList;
import java.util.List;

import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;

/**
 * Restricted-access attraction: individual tickets only, each needing an unconsumed Special Pass.
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
    protected boolean acceptsSpeedPasses() {
        return false;
    }

    /**
     * The Special Pass of the ticket is used automatically.
     */
    @Override
    protected List<Pass> passesToConsume(IndividualAccess ticket, PassLookup passes, boolean speed)
            throws ParkException {
        List<Pass> selected = new ArrayList<>();
        for (IndividualTicket individual : ticket.individualTickets()) {
            selected.add(passes.passesOf(individual).stream()
                    .filter(p -> p instanceof SpecialPass && !p.isConsumed()).findFirst()
                    .orElseThrow(() -> new OperationNotAllowedException(
                            "Ticket " + individual.getId() + " has no unconsumed Special Pass")));
        }
        return selected;
    }
}
