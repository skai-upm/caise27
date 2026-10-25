package es.upm.etsisi.park.business.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Common abstraction for building invoices step by step. Concrete builders add the steps that
 * make sense for their recipient type; sections without lines are never added.
 */
public abstract class InvoiceBuilder {

    private final String title;
    private LocalDate issueDate;
    private final Map<String, String> recipient = new LinkedHashMap<>();
    private final List<InvoiceSection> sections = new ArrayList<>();

    protected InvoiceBuilder(String title) {
        this.title = title;
    }

    public InvoiceBuilder issuedOn(LocalDate date) {
        this.issueDate = date;
        return this;
    }

    protected void addRecipientField(String label, String value) {
        recipient.put(label, value);
    }

    protected void addSection(String sectionTitle, List<InvoiceLine> lines) {
        if (!lines.isEmpty()) {
            sections.add(new InvoiceSection(sectionTitle, lines));
        }
    }

    /**
     * Creates the invoice; the total is the sum of the priced lines.
     */
    public Invoice build() {
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (InvoiceSection section : sections) {
            for (InvoiceLine line : section.lines()) {
                if (line.isPriced()) {
                    total = total.add(line.amount());
                }
            }
        }
        return new Invoice(title, issueDate, recipient, sections, total);
    }
}
