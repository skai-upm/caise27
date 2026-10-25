package es.upm.parque.business.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Default invoice builder.
 */
public class StandardInvoiceBuilder implements InvoiceBuilder {

    private String recipient;
    private LocalDate issueDate;
    private final List<InvoiceSection> sections = new ArrayList<>();
    private String currentTitle;
    private List<InvoiceLine> currentLines;

    public StandardInvoiceBuilder() {
        reset();
    }

    @Override
    public void reset() {
        recipient = "";
        issueDate = null;
        sections.clear();
        currentTitle = null;
        currentLines = null;
    }

    @Override
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    @Override
    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    @Override
    public void startSection(String title) {
        closeSection();
        currentTitle = title;
        currentLines = new ArrayList<>();
    }

    @Override
    public void addItem(String description, BigDecimal price) {
        if (currentLines == null) {
            throw new IllegalStateException("startSection must be called before addItem");
        }
        currentLines.add(new InvoiceLine(description, price));
    }

    @Override
    public Invoice getResult() {
        closeSection();
        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceSection section : sections) {
            for (InvoiceLine line : section.lines()) {
                total = total.add(line.price());
            }
        }
        return new Invoice(recipient, issueDate, sections, total);
    }

    private void closeSection() {
        if (currentTitle != null && !currentLines.isEmpty()) {
            sections.add(new InvoiceSection(currentTitle, currentLines));
        }
        currentTitle = null;
        currentLines = null;
    }
}
