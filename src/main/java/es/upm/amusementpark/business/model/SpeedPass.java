package es.upm.amusementpark.business.model;

import java.math.BigDecimal;

/** Pass that lets its ticket holder use the fast lane once. */
public class SpeedPass extends Pass {

    public SpeedPass(String id, BigDecimal price, boolean consumed) {
        super(id, price, consumed);
    }

    @Override
    public String getKind() {
        return "SpeedPass";
    }
}
