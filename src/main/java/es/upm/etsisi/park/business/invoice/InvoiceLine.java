package es.upm.etsisi.park.business.invoice;

import java.math.BigDecimal;

/**
 * One invoice line. Lines without an amount are informational and do not count towards the total.
 *
 * @param description text shown for the item
 * @param amount      price of the item, or {@code null} for informational lines
 */
public record InvoiceLine(String description, BigDecimal amount) {

    public static InvoiceLine priced(String description, BigDecimal amount) {
        return new InvoiceLine(description, amount);
    }

    public static InvoiceLine informational(String description) {
        return new InvoiceLine(description, null);
    }

    public boolean isPriced() {
        return amount != null;
    }
}
