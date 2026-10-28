package es.upm.amusementpark.business.model;

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
    protected void checkAccess(IndividualAccess ticket, PassLookup passes) throws ParkException {
        for (IndividualTicket individual : ticket.individualTickets()) {
            boolean hasPass = passes.passesOf(individual).stream()
                    .anyMatch(p -> p instanceof SpecialPass && !p.isConsumed());
            if (!hasPass) {
                throw new OperationNotAllowedException("Ticket " + individual.getId() + " has no unconsumed Special Pass");
            }
        }
    }
}
