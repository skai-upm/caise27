package es.upm.etsisi.park.business.invoice;

import es.upm.etsisi.park.business.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Immutable invoice produced by an {@link InvoiceBuilder}; never persisted.
 */
public final class Invoice {

    private final String title;
    private final List<String> recipient;
    private final List<InvoiceSection> sections;

    Invoice(String title, List<String> recipient, List<InvoiceSection> sections) {
        this.title = title;
        this.recipient = List.copyOf(recipient);
        this.sections = List.copyOf(sections);
    }

    public String getTitle() {
        return title;
    }

    public List<String> getRecipient() {
        return recipient;
    }

    public List<InvoiceSection> getSections() {
        return sections;
    }

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceSection s : sections) {
            for (InvoiceLine l : s.lines()) {
                total = total.add(l.charged());
            }
        }
        return Money.of(total);
    }

    public String render() {
        List<String> out = new ArrayList<>();
        out.add("=== " + title + " ===");
        out.addAll(recipient);
        for (InvoiceSection s : sections) {
            out.add(s.title() + ":");
            s.lines().forEach(l -> out.add(l.render()));
        }
        out.add("TOTAL: " + Money.format(getTotal()));
        return String.join(System.lineSeparator(), out);
    }
}
