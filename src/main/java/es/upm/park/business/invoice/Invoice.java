package es.upm.park.business.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * An immutable invoice produced by an {@link InvoiceBuilder}. Only sections
 * with items are present; the total is the sum of the charged items.
 */
public record Invoice(String title, LocalDate issueDate, List<String> recipient, List<InvoiceSection> sections,
                      BigDecimal total) {

    public Invoice {
        recipient = List.copyOf(recipient);
        sections = List.copyOf(sections);
    }
}
