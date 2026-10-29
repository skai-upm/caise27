package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.Objects;

/** One line of an invoice section; descriptive lines have no amount. */
public final class InvoiceLine {

    private final String description;
    private final BigDecimal amount;

    private InvoiceLine(String description, BigDecimal amount) {
        this.description = Objects.requireNonNull(description);
        this.amount = amount;
    }

    public static InvoiceLine item(String description, BigDecimal amount) {
        return new InvoiceLine(description, Objects.requireNonNull(amount));
    }

    public static InvoiceLine detail(String description) {
        return new InvoiceLine(description, null);
    }

    public String getDescription() {
        return description;
    }

    /** @return the amount charged, or {@code null} for descriptive lines */
    public BigDecimal getAmount() {
        return amount;
    }

    public boolean isCharged() {
        return amount != null;
    }
}
