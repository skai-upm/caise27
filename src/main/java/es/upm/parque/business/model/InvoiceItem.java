package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * One charged line of an invoice: what was bought, its amount, and optional descriptive details without amount.
 */
public final class InvoiceItem {

    private final String description;
    private final BigDecimal amount;
    private final List<String> details;

    public InvoiceItem(String description, BigDecimal amount, List<String> details) {
        this.description = Objects.requireNonNull(description);
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.details = List.copyOf(details);
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    /** Descriptive lines (for example the composition of a group ticket); they are never added to the total. */
    public List<String> getDetails() {
        return details;
    }
}
