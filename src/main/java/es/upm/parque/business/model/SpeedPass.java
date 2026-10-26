package es.upm.parque.business.model;

import java.math.BigDecimal;

/**
 * A SpeedPass. Its price is 20% of the price stored in its ticket.
 */
public class SpeedPass extends Pass {

    public SpeedPass(String id, BigDecimal price, PassStatus status) {
        super(id, price, status);
    }

    @Override
    public String getTypeName() {
        return "SpeedPass";
    }
}
