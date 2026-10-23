package es.upm.amusementpark.business.model;

import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import java.util.List;

/**
 * Accepts individual tickets only, optionally with one SpeedPass.
 */
public class IndividualAttraction extends Attraction<IndividualAccess> {

    public IndividualAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.INDIVIDUAL;
    }

    @Override
    public List<Pass> requiredPasses(IndividualAccess ticket, boolean speed, PassWallet wallet)
            throws OperationNotAllowedException {
        return speed ? oneSpeedPassEach(List.of(ticket), wallet) : List.of();
    }
}
