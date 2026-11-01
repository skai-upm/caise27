package es.upm.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * A personal pass attached to an individual ticket. Consumed passes stay recorded.
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

    public void consume() {
        consumed = true;
    }

    /**
     * Undoes a consumption whose change could not be persisted.
     */
    public void restore() {
        consumed = false;
    }

    /**
     * Name of the pass kind as shown to users.
     */
    public abstract String getKind();
}
