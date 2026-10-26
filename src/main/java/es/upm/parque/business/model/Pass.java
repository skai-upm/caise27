package es.upm.parque.business.model;

import java.math.BigDecimal;

/**
 * A personal pass attached to an individual ticket. Consumed passes stay in the system with their status.
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
        if (consumed) {
            throw new IllegalArgumentException("pass " + id + " has already been consumed");
        }
        consumed = true;
    }

    /** Human-readable name of the pass kind. */
    public abstract String getKind();

    @Override
    public String toString() {
        return id + " (" + getKind() + ", " + price.toPlainString() + (consumed ? ", consumed" : "") + ")";
    }
}
