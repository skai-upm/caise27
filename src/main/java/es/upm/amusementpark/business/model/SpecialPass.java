package es.upm.amusementpark.business.model;

import java.math.BigDecimal;

/** Pass required to enter SPECIAL attractions; at most one per individual ticket in its lifetime. */
public class SpecialPass extends Pass {

    public SpecialPass(String id, BigDecimal price, boolean consumed) {
        super(id, price, consumed);
    }

    @Override
    public String getKind() {
        return "Special Pass";
    }
}
