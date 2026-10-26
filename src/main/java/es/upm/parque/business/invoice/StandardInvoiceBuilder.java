package es.upm.parque.business.invoice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Default invoice builder: sections without items are left out and the total is the sum of all items.
 */
public class StandardInvoiceBuilder implements InvoiceBuilder {

    private String recipient;
    private final List<String> titles = new ArrayList<>();
    private final List<List<InvoiceLine>> items = new ArrayList<>();

    @Override
    public InvoiceBuilder recipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    @Override
    public InvoiceBuilder section(String title) {
        titles.add(title);
        items.add(new ArrayList<>());
        return this;
    }

    @Override
    public InvoiceBuilder item(String description, BigDecimal amount) {
        if (items.isEmpty()) {
            throw new IllegalStateException("An item must belong to a section");
        }
        items.get(items.size() - 1).add(new InvoiceLine(description, amount));
        return this;
    }

    @Override
    public Invoice build() {
        List<InvoiceSection> sections = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (int i = 0; i < titles.size(); i++) {
            List<InvoiceLine> lines = items.get(i);
            if (!lines.isEmpty()) {
                sections.add(new InvoiceSection(titles.get(i), lines));
                for (InvoiceLine line : lines) {
                    total = total.add(line.amount());
                }
            }
        }
        Invoice invoice = new Invoice(recipient, sections, total);
        recipient = null;
        titles.clear();
        items.clear();
        return invoice;
    }
}
