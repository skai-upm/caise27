package es.upm.amusementpark.business;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Personal pass bought for an individual ticket. The pass knows its ticket; tickets do not know their passes.
 * Consumed passes remain as purchase records.
 */
public abstract class Pass {

    private final String id;
    private final IndividualTicket ticket;
    private final BigDecimal price;
    private boolean consumed;

    /**
     * @param price price fixed at purchase (stored with the pass)
     */
    protected Pass(String id, IndividualTicket ticket, BigDecimal price) {
        this.id = Objects.requireNonNull(id, "id");
        this.ticket = Objects.requireNonNull(ticket, "ticket");
        this.price = Objects.requireNonNull(price, "price");
    }

    public String getId() {
        return id;
    }

    public IndividualTicket getTicket() {
        return ticket;
    }

    public boolean isConsumed() {
        return consumed;
    }

    void markConsumed() {
        consumed = true;
    }

    /** @return the price stored at purchase, computed from the associated ticket's price */
    public BigDecimal getPrice() {
        return price;
    }

    /** @return human-readable pass type, for example {@code SpeedPass} */
    public abstract String getTypeName();
}
