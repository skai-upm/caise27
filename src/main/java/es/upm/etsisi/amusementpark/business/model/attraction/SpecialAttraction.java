package es.upm.etsisi.amusementpark.business.model.attraction;

import es.upm.etsisi.amusementpark.business.exception.ForbiddenOperationException;
import es.upm.etsisi.amusementpark.business.exception.ParkException;
import es.upm.etsisi.amusementpark.business.model.IndividualAccess;
import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.PassLookup;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import java.util.ArrayList;
import java.util.List;

/**
 * Restricted-access attraction accepting individual tickets only. Entry automatically consumes the
 * ticket's Special Pass; SpeedPasses are not accepted.
 */
public class SpecialAttraction extends Attraction<IndividualAccess> {

    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }

    @Override
    protected Class<IndividualAccess> accessType() {
        return IndividualAccess.class;
    }

    @Override
    protected List<Pass> requiredPasses(IndividualAccess access, boolean speed, PassLookup passes)
            throws ParkException {
        if (speed) {
            throw new ForbiddenOperationException("special attraction " + getId() + " does not accept SpeedPasses");
        }
        List<Pass> required = new ArrayList<>();
        for (IndividualTicket ticket : access.entryTickets()) {
            SpecialPass pass = passes.specialPassOf(ticket);
            if (pass == null) {
                throw new ForbiddenOperationException("ticket " + ticket.getId() + " has no Special Pass");
            }
            if (pass.isConsumed()) {
                throw new ForbiddenOperationException("Special Pass " + pass.getId() + " is already consumed");
            }
            required.add(pass);
        }
        return required;
    }
}
