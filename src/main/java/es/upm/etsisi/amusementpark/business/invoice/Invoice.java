package es.upm.etsisi.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * An immutable invoice produced by an {@link InvoiceBuilder}.
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
        return sections.stream().filter(InvoiceSection::charged).map(InvoiceSection::subtotal)
                .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    public String toText() {
        List<String> lines = new ArrayList<>();
        lines.add("===== " + title + " =====");
        lines.addAll(recipient);
        for (InvoiceSection section : sections) {
            lines.add("-- " + section.title() + (section.charged() ? "" : " (included above, not charged again)"));
            for (InvoiceItem item : section.items()) {
                lines.add("   " + item.description() + " : " + item.amount() + " EUR");
            }
        }
        lines.add("TOTAL: " + getTotal() + " EUR");
        return String.join(System.lineSeparator(), lines);
    }
}
