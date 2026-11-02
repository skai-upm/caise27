package es.upm.etsisi.amusementpark.business.attraction;

import es.upm.etsisi.amusementpark.business.BusinessException;
import es.upm.etsisi.amusementpark.business.OperationNotAllowedException;
import es.upm.etsisi.amusementpark.business.pass.Pass;
import es.upm.etsisi.amusementpark.business.pass.SpecialPass;
import es.upm.etsisi.amusementpark.business.ticket.IndividualAccess;
import java.util.List;

/**
 * Accepts individual tickets that hold an unconsumed Special Pass, which entry consumes
 * automatically. SpeedPasses are not accepted.
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
    protected List<Pass> selectPasses(IndividualAccess ticket, boolean speed, PassBook passes)
            throws BusinessException {
        if (speed) {
            throw new OperationNotAllowedException("SPECIAL attraction " + getId() + " does not accept SpeedPasses");
        }
        SpecialPass pass = passes.specialPass(ticket.individualTicket()).filter(p -> !p.isConsumed())
                .orElseThrow(() -> new OperationNotAllowedException(
                        "ticket " + ticket.getId() + " has no unconsumed Special Pass"));
        return List.of(pass);
    }
}
