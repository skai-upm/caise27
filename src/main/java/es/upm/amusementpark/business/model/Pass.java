package es.upm.amusementpark.business.model;

import java.math.BigDecimal;

/** A personal pass bought for an individual ticket; it keeps its price and whether it has been used. */
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

    /** Short name of the pass kind, as shown to users. */
    public abstract String getKind();
}
