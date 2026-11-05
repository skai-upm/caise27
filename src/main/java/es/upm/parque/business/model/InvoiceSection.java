package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * A titled group of invoice items, such as the individual tickets of a user or the group tickets of a group.
 */
public final class InvoiceSection {

    private final String heading;
    private final List<InvoiceItem> items;

    public InvoiceSection(String heading, List<InvoiceItem> items) {
        this.heading = Objects.requireNonNull(heading);
        this.items = List.copyOf(items);
    }

    public String getHeading() {
        return heading;
    }

    public List<InvoiceItem> getItems() {
        return items;
    }

    public BigDecimal getSubtotal() {
        return items.stream().map(InvoiceItem::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
