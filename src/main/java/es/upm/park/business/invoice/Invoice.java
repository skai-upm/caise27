package es.upm.park.business.invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * An immutable invoice made of recipient lines and sections of items. The total
 * is the sum of the item amounts shown; descriptive details never add to it.
 */
public final class Invoice {

    /** One invoiced item; {@code amount} is null for purely descriptive entries. */
    public record Item(String description, BigDecimal amount, List<String> details) {
    }

    /** A titled group of items. */
    public record Section(String title, List<Item> items) {
    }

    private final String title;
    private final List<String> recipient;
    private final List<Section> sections;

    Invoice(String title, List<String> recipient, List<Section> sections) {
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

    public List<Section> getSections() {
        return sections;
    }

    public BigDecimal total() {
        return sections.stream()
                .flatMap(section -> section.items().stream())
                .map(Item::amount)
                .filter(amount -> amount != null)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);
    }

    /** Legible text form of the invoice. */
    public String render() {
        String nl = System.lineSeparator();
        StringBuilder out = new StringBuilder(title);
        recipient.forEach(line -> out.append(nl).append(line));
        for (Section section : sections) {
            out.append(nl).append(section.title()).append(':');
            for (Item item : section.items()) {
                out.append(nl).append("  ").append(item.description());
                if (item.amount() != null) {
                    out.append(' ').append(item.amount());
                }
                item.details().forEach(detail -> out.append(nl).append("    ").append(detail));
            }
        }
        return out.append(nl).append("TOTAL: ").append(total()).toString();
    }
}
