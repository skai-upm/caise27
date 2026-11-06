package es.upm.park.business.invoice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * An immutable invoice produced by an {@link InvoiceBuilder}.
 */
public final class Invoice {

    private final String type;
    private final List<String> recipient;
    private final List<InvoiceSection> sections;
    private final BigDecimal total;

    Invoice(String type, List<String> recipient, List<InvoiceSection> sections, BigDecimal total) {
        this.type = type;
        this.recipient = List.copyOf(recipient);
        this.sections = List.copyOf(sections);
        this.total = total;
    }

    public String getType() {
        return type;
    }

    public List<String> getRecipient() {
        return recipient;
    }

    public List<InvoiceSection> getSections() {
        return sections;
    }

    public BigDecimal getTotal() {
        return total;
    }

    /** Legible text form of the invoice. */
    public String render() {
        List<String> out = new ArrayList<>();
        out.add("INVOICE (" + type + ")");
        out.addAll(recipient);
        for (InvoiceSection section : sections) {
            out.add(section.title() + ":");
            for (InvoiceLine line : section.lines()) {
                out.add(line.amount().map(a -> "  " + line.description() + "  " + a.toPlainString())
                        .orElse("      " + line.description()));
            }
        }
        out.add("TOTAL: " + total.toPlainString());
        return String.join(System.lineSeparator(), out);
    }
}
