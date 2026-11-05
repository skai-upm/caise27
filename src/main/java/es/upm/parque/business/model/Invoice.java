package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * An invoice generated on demand (never persisted). It holds only the sections a builder actually produced;
 * its total is the sum of the amounts of the items shown.
 */
public final class Invoice {

    private final String title;
    private final List<String> recipient;
    private final List<InvoiceSection> sections;

    public Invoice(String title, List<String> recipient, List<InvoiceSection> sections) {
        this.title = Objects.requireNonNull(title);
        this.recipient = List.copyOf(recipient);
        this.sections = List.copyOf(sections);
    }

    public String getTitle() {
        return title;
    }

    /** Identifying lines of the invoiced user or group. */
    public List<String> getRecipient() {
        return recipient;
    }

    public List<InvoiceSection> getSections() {
        return sections;
    }

    public BigDecimal getTotal() {
        return sections.stream().map(InvoiceSection::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
