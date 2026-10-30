package es.upm.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * Personal pass attached to an individual ticket, with its own identifier, purchase price and consumption state.
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

    /** Marks the pass as used. Consumed passes stay recorded. */
    public void consume() {
        consumed = true;
    }

    /** Reverts a consumption that could not be completed. */
    public void restore() {
        consumed = false;
    }

    /** Human-readable kind of pass, e.g. {@code SpeedPass}. */
    public abstract String getKind();
}
