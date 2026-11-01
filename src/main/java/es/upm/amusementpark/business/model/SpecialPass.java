package es.upm.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * Pass required to enter a SPECIAL attraction once.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String id, BigDecimal price, boolean consumed) {
        super(id, price, consumed);
    }

    @Override
    public String getKind() {
        return "Special Pass";
    }
}
