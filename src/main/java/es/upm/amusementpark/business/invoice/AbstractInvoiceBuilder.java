package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Parts shared by every invoice: header, recipient lines, non-empty sections and the total of the items shown.
 */
public abstract class AbstractInvoiceBuilder implements InvoiceBuilder {

    private final LocalDate issueDate;
    private String title;
    private List<String> recipient;
    private List<InvoiceSection> sections;
    private BigDecimal total;

    protected AbstractInvoiceBuilder(LocalDate issueDate) {
        this.issueDate = issueDate;
        reset();
    }

    @Override
    public void reset() {
        title = "";
        recipient = new ArrayList<>();
        sections = new ArrayList<>();
        total = BigDecimal.ZERO.setScale(2);
    }

    protected void setTitle(String title) {
        this.title = title;
    }

    protected void addRecipientLine(String line) {
        recipient.add(line);
    }

    /**
     * Adds the section only if it has lines, so invoices never show artificially empty sections.
     */
    protected void addSection(String sectionTitle, List<InvoiceLine> lines) {
        if (!lines.isEmpty()) {
            sections.add(new InvoiceSection(sectionTitle, lines));
        }
    }

    @Override
    public void buildTotal() {
        BigDecimal sum = BigDecimal.ZERO.setScale(2);
        for (InvoiceSection section : sections) {
            for (InvoiceLine line : section.lines()) {
                sum = sum.add(line.amount());
            }
        }
        total = sum;
    }

    @Override
    public Invoice getResult() {
        return new Invoice(title, issueDate, recipient, sections, total);
    }
}
