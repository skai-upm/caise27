package es.upm.parque.business.model;

import java.math.BigDecimal;

/**
 * A personal pass attached to an individual ticket. It keeps its own price and status; used and refunded passes
 * stay in the system with that status.
 */
public abstract class Pass {

    private final String id;
    private final BigDecimal price;
    private PassStatus status;

    protected Pass(String id, BigDecimal price, PassStatus status) {
        this.id = id;
        this.price = price;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public PassStatus getStatus() {
        return status;
    }

    public boolean isAvailable() {
        return status == PassStatus.AVAILABLE;
    }

    /** Marks the pass as used; it stays attached to its ticket. */
    public void consume() {
        requireAvailable();
        status = PassStatus.CONSUMED;
    }

    /** Marks the pass as refunded; it stays attached to its ticket as history. */
    public void refund() {
        requireAvailable();
        status = PassStatus.REFUNDED;
    }

    /** @return a short name of the pass type, used in messages and invoices */
    public abstract String getTypeName();

    private void requireAvailable() {
        if (!isAvailable()) {
            throw new IllegalArgumentException(getTypeName() + " " + id + " is " + status + ", not AVAILABLE");
        }
    }
}
