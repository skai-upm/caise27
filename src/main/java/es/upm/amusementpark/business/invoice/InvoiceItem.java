package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * One charged line of an invoice. Details describe the item and are not
 * charged again.
 *
 * @param description what was bought
 * @param amount      the price charged for it
 * @param details     descriptive lines, such as the tickets inside a group ticket
 */
public record InvoiceItem(String description, BigDecimal amount, List<String> details) {

    public InvoiceItem {
        details = List.copyOf(details);
    }

    public InvoiceItem(String description, BigDecimal amount) {
        this(description, amount, List.of());
    }
}
