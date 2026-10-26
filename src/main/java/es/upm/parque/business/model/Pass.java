package es.upm.parque.business.model;

import es.upm.parque.business.ParkException;
import java.math.BigDecimal;

/**
 * A personal pass attached to an individual ticket. It keeps its price and whether it has been
 * consumed; consumed passes are not removed.
 */
public abstract class Pass {

    private final String id;
    private final BigDecimal price;
    private boolean consumed;

    protected Pass(String id, BigDecimal price, boolean consumed) {
        this.id = id;
        this.price = price;
        this.consumed = consumed;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isConsumed() {
        return consumed;
    }

    /**
     * Marks the pass as used; a pass can only be consumed once.
     */
    public void consume() {
        if (consumed) {
            throw new ParkException("Pass " + id + " has already been consumed");
        }
        consumed = true;
    }

    /**
     * Name of the kind of pass, as shown to users.
     */
    public abstract String getKind();
}
