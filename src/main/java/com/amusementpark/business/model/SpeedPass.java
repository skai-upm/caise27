package com.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * Pass for priority access; each one admits once.
 */
public class SpeedPass extends Pass {

    public SpeedPass(String id, BigDecimal price, boolean consumed) {
        super(id, price, consumed);
    }

    @Override
    public String getKind() {
        return "SpeedPass";
    }
}
