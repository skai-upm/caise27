package es.upm.etsisi.park.business.model;

import es.upm.etsisi.park.business.exception.AccessDeniedException;
import java.util.List;

/**
 * Restricted-access attraction: individual tickets only, each entry consumes the ticket's
 * Special Pass automatically, and SpeedPasses are not accepted.
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
    protected List<Pass> passesToConsume(IndividualAccess ticket, boolean speed) {
        if (speed) {
            throw new AccessDeniedException("Special attraction " + getId() + " does not accept SpeedPasses");
        }
        IndividualTicket individual = ticket.getIndividualTicket();
        SpecialPass pass = individual.getSpecialPass()
                .filter(p -> !p.isConsumed())
                .orElseThrow(() -> new AccessDeniedException("Ticket " + individual.getId()
                        + " has no unconsumed Special Pass"));
        return List.of(pass);
    }
}
