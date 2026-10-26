package es.upm.parque.business.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Standard invoice builder: keeps the header and the sections added so far and computes the total at the end.
 */
public class DefaultInvoiceBuilder implements InvoiceBuilder {
    private String recipientType;
    private String recipientId;
    private String recipientName;
    private LocalDate issueDate;
    private final List<InvoiceSection> sections = new ArrayList<>();

    @Override
    public void reset() {
        recipientType = null;
        recipientId = null;
        recipientName = null;
        issueDate = null;
        sections.clear();
    }

    @Override
    public void buildHeader(String recipientType, String recipientId, String recipientName, LocalDate issueDate) {
        this.recipientType = recipientType;
        this.recipientId = recipientId;
        this.recipientName = recipientName;
        this.issueDate = issueDate;
    }

    @Override
    public void buildSection(String title, List<InvoiceLine> lines) {
        sections.add(new InvoiceSection(title, lines));
    }

    @Override
    public Invoice getResult() {
        if (recipientType == null) {
            throw new IllegalStateException("the invoice header has not been built");
        }
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (InvoiceSection s : sections) {
            total = total.add(s.getSubtotal());
        }
        return new Invoice(recipientType, recipientId, recipientName, issueDate, sections, total);
    }
}
