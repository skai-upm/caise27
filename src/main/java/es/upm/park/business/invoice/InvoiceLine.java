package es.upm.park.business.invoice;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * One line of an invoice section: a priced item or a non-priced detail.
 */
public record InvoiceLine(String description, Optional<BigDecimal> amount) {

    public static InvoiceLine item(String description, BigDecimal amount) {
        return new InvoiceLine(description, Optional.of(amount));
    }

    public static InvoiceLine detail(String description) {
        return new InvoiceLine(description, Optional.empty());
    }

    public boolean isPriced() {
        return amount.isPresent();
    }
}
