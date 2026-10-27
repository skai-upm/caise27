package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Concrete builder used for both user and group invoices.
 */
public class StandardInvoiceBuilder implements InvoiceBuilder {

    private Invoice.Type type;
    private final List<Map.Entry<String, String>> recipient = new ArrayList<>();
    private final List<String> sectionTitles = new ArrayList<>();
    private final List<List<PendingItem>> sectionItems = new ArrayList<>();

    private static final class PendingItem {
        private final String description;
        private final BigDecimal amount;
        private final List<String> details = new ArrayList<>();

        private PendingItem(String description, BigDecimal amount) {
            this.description = description;
            this.amount = amount;
        }
    }

    @Override
    public InvoiceBuilder forRecipient(Invoice.Type type) {
        this.type = type;
        recipient.clear();
        sectionTitles.clear();
        sectionItems.clear();
        return this;
    }

    @Override
    public InvoiceBuilder recipient(String label, String value) {
        recipient.add(new AbstractMap.SimpleImmutableEntry<>(label, value));
        return this;
    }

    @Override
    public InvoiceBuilder section(String title) {
        sectionTitles.add(title);
        sectionItems.add(new ArrayList<>());
        return this;
    }

    @Override
    public InvoiceBuilder item(String description, BigDecimal amount) {
        if (sectionItems.isEmpty()) {
            throw new IllegalStateException("Open a section before adding items");
        }
        sectionItems.get(sectionItems.size() - 1).add(new PendingItem(description, amount));
        return this;
    }

    @Override
    public InvoiceBuilder detail(String text) {
        List<PendingItem> current = sectionItems.isEmpty() ? List.of() : sectionItems.get(sectionItems.size() - 1);
        if (current.isEmpty()) {
            throw new IllegalStateException("Add an item before its details");
        }
        current.get(current.size() - 1).details.add(text);
        return this;
    }

    @Override
    public Invoice build() {
        if (type == null) {
            throw new IllegalStateException("Recipient type not set");
        }
        List<InvoiceSection> sections = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (int i = 0; i < sectionTitles.size(); i++) {
            List<InvoiceItem> items = new ArrayList<>();
            for (PendingItem p : sectionItems.get(i)) {
                items.add(new InvoiceItem(p.description, p.amount, p.details));
                total = total.add(p.amount);
            }
            if (!items.isEmpty()) {
                sections.add(new InvoiceSection(sectionTitles.get(i), items));
            }
        }
        return new Invoice(type, recipient, sections, total);
    }
}
