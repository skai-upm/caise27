package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * A line of an invoice: either a charged item, counted in the total, or a descriptive detail.
 */
public final class InvoiceItem {

    private final String description;
    private final BigDecimal amount;

    private InvoiceItem(String description, BigDecimal amount) {
        this.description = description;
        this.amount = amount;
    }

    public static InvoiceItem charged(String description, BigDecimal amount) {
        return new InvoiceItem(description, amount);
    }

    public static InvoiceItem detail(String description) {
        return new InvoiceItem(description, null);
    }

    public String getDescription() {
        return description;
    }

    /**
     * Amount charged, absent for descriptive details.
     */
    public Optional<BigDecimal> getAmount() {
        return Optional.ofNullable(amount);
    }
}
