package es.upm.park.business.invoice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds {@link Invoice} objects. Sections that received no lines are
 * omitted and the total is the sum of the priced items.
 */
public class TextInvoiceBuilder implements InvoiceBuilder {

    private String type;
    private final List<String> recipient = new ArrayList<>();
    private final Map<String, List<InvoiceLine>> sections = new LinkedHashMap<>();
    private List<InvoiceLine> current;

    public TextInvoiceBuilder() {
        reset("INVOICE");
    }

    @Override
    public InvoiceBuilder reset(String invoiceType) {
        this.type = invoiceType;
        recipient.clear();
        sections.clear();
        current = null;
        return this;
    }

    @Override
    public InvoiceBuilder recipient(String line) {
        recipient.add(line);
        return this;
    }

    @Override
    public InvoiceBuilder section(String title) {
        current = sections.computeIfAbsent(title, t -> new ArrayList<>());
        return this;
    }

    @Override
    public InvoiceBuilder item(String description, BigDecimal amount) {
        lines().add(InvoiceLine.item(description, amount));
        return this;
    }

    @Override
    public InvoiceBuilder detail(String text) {
        lines().add(InvoiceLine.detail(text));
        return this;
    }

    @Override
    public Invoice build() {
        List<InvoiceSection> built = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (Map.Entry<String, List<InvoiceLine>> e : sections.entrySet()) {
            if (!e.getValue().isEmpty()) {
                built.add(new InvoiceSection(e.getKey(), e.getValue()));
                for (InvoiceLine line : e.getValue()) {
                    total = total.add(line.amount().orElse(BigDecimal.ZERO));
                }
            }
        }
        return new Invoice(type, recipient, built, total);
    }

    private List<InvoiceLine> lines() {
        if (current == null) {
            throw new IllegalStateException("open a section before adding lines");
        }
        return current;
    }
}
