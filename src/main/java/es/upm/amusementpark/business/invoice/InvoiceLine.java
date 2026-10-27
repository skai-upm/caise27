package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * One item of an invoice. Detail lines describe the item further; items that
 * do not count in the total are shown for information only.
 */
public record InvoiceLine(String description, BigDecimal amount, List<String> details, boolean countsInTotal) {

    public InvoiceLine {
        details = List.copyOf(details);
    }

    public static InvoiceLine charged(String description, BigDecimal amount) {
        return new InvoiceLine(description, amount, List.of(), true);
    }
}
