package com.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * Pass required by SPECIAL attractions; at most one per individual ticket. It costs the ticket price.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String id, BigDecimal price, boolean consumed) {
        super(id, price, consumed);
    }
}
