package es.upm.parque.business.model;

import java.math.BigDecimal;

/**
 * The Special Pass of a ticket. It costs the price stored in its ticket; a ticket gets at most one in its lifetime.
 */
public class SpecialPass extends Pass {

    public SpecialPass(String id, BigDecimal price, PassStatus status) {
        super(id, price, status);
    }

    @Override
    public String getTypeName() {
        return "SpecialPass";
    }
}
