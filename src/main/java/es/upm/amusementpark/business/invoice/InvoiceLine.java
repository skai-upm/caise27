package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;

/**
 * One line of an invoice. Lines that are not counted are descriptive detail and do not add to the total.
 */
public record InvoiceLine(String description, BigDecimal amount, boolean counted) {

    public static InvoiceLine item(String description, BigDecimal amount) {
        return new InvoiceLine(description, amount, true);
    }

    public static InvoiceLine detail(String description) {
        return new InvoiceLine(description, null, false);
    }
}
