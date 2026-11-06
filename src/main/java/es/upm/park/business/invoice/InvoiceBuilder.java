package es.upm.park.business.invoice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Common abstraction for building invoices step by step. Concrete builders add
 * the steps that make sense for their recipient type. Sections that end up
 * without items are omitted from the built invoice.
 *
 * @param <B> the concrete builder type, so that steps can be chained
 */
public abstract class InvoiceBuilder<B extends InvoiceBuilder<B>> {

    private String title = "INVOICE";
    private final List<String> recipient = new ArrayList<>();
    private final List<SectionDraft> sections = new ArrayList<>();

    private static final class SectionDraft {
        private final String title;
        private final List<Invoice.Item> items = new ArrayList<>();

        private SectionDraft(String title) {
            this.title = title;
        }
    }

    protected abstract B self();

    public B title(String text) {
        this.title = text;
        return self();
    }

    public B recipient(String label, String value) {
        recipient.add(label + ": " + value);
        return self();
    }

    public B startSection(String sectionTitle) {
        sections.add(new SectionDraft(sectionTitle));
        return self();
    }

    /** Adds a priced item to the current section. */
    public B item(String description, BigDecimal amount) {
        return addItem(description, amount, List.of());
    }

    /** Adds a priced item with descriptive detail lines to the current section. */
    public B item(String description, BigDecimal amount, List<String> details) {
        return addItem(description, amount, details);
    }

    private B addItem(String description, BigDecimal amount, List<String> details) {
        if (sections.isEmpty()) {
            throw new IllegalStateException("startSection must be called before adding items");
        }
        sections.get(sections.size() - 1).items.add(new Invoice.Item(description, amount, List.copyOf(details)));
        return self();
    }

    public Invoice build() {
        List<Invoice.Section> nonEmpty = sections.stream()
                .filter(section -> !section.items.isEmpty())
                .map(section -> new Invoice.Section(section.title, List.copyOf(section.items)))
                .toList();
        return new Invoice(title, recipient, nonEmpty);
    }
}
