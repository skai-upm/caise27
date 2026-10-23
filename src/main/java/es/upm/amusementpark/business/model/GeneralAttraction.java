package es.upm.amusementpark.business.model;

import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import java.util.List;

/**
 * Accepts individual and group tickets. With SPEED, every entering
 * individual ticket uses one SpeedPass.
 */
public class GeneralAttraction extends Attraction<GeneralAccess> {

    public GeneralAttraction(String id, String name, int capacity) {
        super(id, name, capacity, GeneralAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GENERAL;
    }

    @Override
    public List<Pass> requiredPasses(GeneralAccess ticket, boolean speed, PassWallet wallet)
            throws OperationNotAllowedException {
        return speed ? oneSpeedPassEach(ticket.getEntryTickets(), wallet) : List.of();
    }
}
