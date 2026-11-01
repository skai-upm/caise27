package es.upm.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * Pass required to enter special attractions. It costs the price stored in its
 * ticket, and a ticket can only ever receive one.
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
