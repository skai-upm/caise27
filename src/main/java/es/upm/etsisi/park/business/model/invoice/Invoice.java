package es.upm.etsisi.park.business.model.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import es.upm.etsisi.park.business.model.ticket.Money;

/**
 * Immutable invoice produced by an {@link InvoiceBuilder}. It contains only
 * the sections that apply to its recipient and have content.
 */
public final class Invoice {

    private static final String RULE = "=".repeat(64);
    private static final int DESCRIPTION_WIDTH = 50;

    private final String title;
    private final LocalDate date;
    private final List<String> recipient;
    private final List<InvoiceSection> sections;
    private final BigDecimal total;

    Invoice(String title, LocalDate date, List<String> recipient, List<InvoiceSection> sections) {
        this.title = title;
        this.date = date;
        this.recipient = List.copyOf(recipient);
        this.sections = List.copyOf(sections);
        BigDecimal sum = BigDecimal.ZERO;
        for (InvoiceSection section : this.sections) {
            sum = sum.add(section.subtotal());
        }
        this.total = Money.of(sum);
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<String> getRecipient() {
        return recipient;
    }

    public List<InvoiceSection> getSections() {
        return sections;
    }

    public InvoiceSection findSection(String sectionTitle) {
        for (InvoiceSection section : sections) {
            if (section.title().equals(sectionTitle)) {
                return section;
            }
        }
        return null;
    }

    public BigDecimal getTotal() {
        return total;
    }

    /** Plain-text rendering of the invoice. */
    public String toText() {
        StringBuilder sb = new StringBuilder();
        sb.append(RULE).append('\n');
        sb.append(title).append('\n');
        sb.append("Date: ").append(date).append('\n');
        for (String line : recipient) {
            sb.append(line).append('\n');
        }
        for (InvoiceSection section : sections) {
            sb.append("-- ").append(section.title()).append('\n');
            for (InvoiceLine line : section.lines()) {
                if (line.isItem()) {
                    sb.append(String.format("   %-" + DESCRIPTION_WIDTH + "s %12s%n", line.description(),
                            Money.format(line.amount())));
                } else {
                    sb.append("       ").append(line.description()).append('\n');
                }
            }
        }
        sb.append(String.format("%-" + (DESCRIPTION_WIDTH + 3) + "s %12s%n", "TOTAL", Money.format(total)));
        sb.append(RULE);
        return sb.toString();
    }
}
