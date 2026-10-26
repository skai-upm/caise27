package es.upm.parque.business.invoice;

import java.math.BigDecimal;

/**
 * One charged item of an invoice.
 */
public class InvoiceLine {
    private final String description;
    private final BigDecimal amount;

    public InvoiceLine(String description, BigDecimal amount) {
        this.description = description;
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
