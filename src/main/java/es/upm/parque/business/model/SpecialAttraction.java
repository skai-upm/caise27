package es.upm.parque.business.model;

import java.util.List;

/**
 * Admits individual tickets only, never with SpeedPasses, and each entry uses the ticket's Special Pass.
 */
public class SpecialAttraction extends Attraction<IndividualAdmission> {

    public static final String NO_SPECIAL_PASS = "ERROR NO SPECIAL PASS";
    public static final String SPECIAL_PASS_CONSUMED = "ERROR SPECIAL PASS CONSUMED";

    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAdmission.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }

    @Override
    public boolean allowsSpeed() {
        return false;
    }

    @Override
    public EntryCheck checkPasses(IndividualAdmission admission) {
        SpecialPass pass = admission.getSpecialPass();
        if (pass == null) {
            return EntryCheck.rejected(NO_SPECIAL_PASS);
        }
        if (pass.isConsumed()) {
            return EntryCheck.rejected(SPECIAL_PASS_CONSUMED);
        }
        return EntryCheck.passes(List.of(pass));
    }
}
