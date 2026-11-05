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
    private boolean consumed;

    protected Pass(String id, IndividualTicket ticket) {
        this.id = Objects.requireNonNull(id, "id");
        this.ticket = Objects.requireNonNull(ticket, "ticket");
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

    /** @return the price, derived from the price stored in the associated ticket */
    public abstract BigDecimal getPrice();

    /** @return human-readable pass type, for example {@code SpeedPass} */
    public abstract String getTypeName();
}
