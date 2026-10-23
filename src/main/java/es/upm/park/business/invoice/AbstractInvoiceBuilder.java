package es.upm.park.business.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared building steps: recipient lines, items grouped into sections in
 * insertion order, and the total. Sections without items never appear.
 */
public abstract class AbstractInvoiceBuilder implements InvoiceBuilder {

    private String title;
    private LocalDate issueDate;
    private final List<String> recipient = new ArrayList<>();
    private final Map<String, List<InvoiceItem>> sections = new LinkedHashMap<>();

    protected AbstractInvoiceBuilder(String title) {
        this.title = title;
    }

    @Override
    public InvoiceBuilder start(LocalDate issueDate) {
        this.issueDate = issueDate;
        recipient.clear();
        sections.clear();
        return this;
    }

    protected void addRecipientLine(String line) {
        recipient.add(line);
    }

    protected void addItem(String section, InvoiceItem item) {
        sections.computeIfAbsent(section, key -> new ArrayList<>()).add(item);
    }

    protected void setTitle(String title) {
        this.title = title;
    }

    @Override
    public Invoice build() {
        if (issueDate == null) {
            throw new IllegalStateException("start() must be called before build()");
        }
        List<InvoiceSection> built = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (Map.Entry<String, List<InvoiceItem>> entry : sections.entrySet()) {
            built.add(new InvoiceSection(entry.getKey(), entry.getValue()));
            for (InvoiceItem item : entry.getValue()) {
                total = total.add(item.amount());
            }
        }
        Invoice invoice = new Invoice(title, issueDate, recipient, built, total);
        issueDate = null;
        return invoice;
    }
}
